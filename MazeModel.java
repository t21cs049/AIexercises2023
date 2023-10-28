import java.io.*;
import java.util.IllegalFormatWidthException;

import javax.swing.tree.FixedHeightLayoutCache;

/**
 * 強化学習によりゴールまでの経路を学習するクラス
 */
public class MazeModel implements Runnable {

	/**
	 * 強化学習によりゴールまでの経路を学習するオブジェクトを生成する
	 * 
	 * @param mazeFile 迷路データのファイル名
	 */
	public MazeModel(String mazeFile) {
		// 迷路データを生成
		mazeData = new MazeData(mazeFile);
		//読み取った迷路データの横、縦の長さを取得する
		height = mazeData.getHeight();
		width = mazeData.getWidth();
		//状態数を計算する
		statesNumber = height*width;
		//行動の選択肢の数を設定する
		actionNumber = 4;
		// ロボットを生成
		robot = new Robot(mazeData.getSX(), mazeData.getSY());
	}

	/**
	 * 実行用関数
	 */
	public void run() {
		try {
			// step 1: Q学習する
			QLearning q1 = new QLearning(statesNumber, actionNumber, 0.5, 0.5);

			int trials = 100; // 強化学習の試行回数
			int steps = 500; // １試行あたりの最大ステップ数
			for (int t = 1; t <= trials; t++) { // 試行回数だけ繰り返し
				/* ロボットを初期位置に戻す */
				robot.setX(mazeData.getSX());
				robot.setY(mazeData.getSY());
				//終了条件を判定する為に前回のQTbaleの更新前のデータを保持しておくTableを用意
				double beforeQTable[][] = new double[statesNumber][actionNumber];
				for (int i = 0; i < beforeQTable.length; i++) {
					for (int j = 0; j < beforeQTable[i].length; j++) {
						//QTableの初期値と比べたときに差が分かりやすく大きくするために１００００で初期化する
						beforeQTable[i][j] = 10000;
					}
				}
				//更新前のQ値と更新後のQ値の誤差が小さくなった回数を１エピソード毎に数えるための変数
				int CountInSameQ = 0;
				for (int s = 0; s < steps; s++) { // ステップ数だけ繰り返し
					/* ε-Greedy 法により行動を選択 */
					// ロボットの現在位置を取得
					int x = robot.getX();
					int y = robot.getY();
					//ロボットが今いる座標から状態情報を取得する
					int state = judgeState(x, y);
					//epsilonを設定する
					double epsilon = 0.5;
					//今のロボットの状態から適切な行動を選択する
					int action = q1.selectAction(state, epsilon);
					/* 選択した行動を実行 (ロボットを移動する) */
					doAction(action, robot);
					/* 新しい状態を観測＆報酬を得る */
				    //更新した座標をx,yに反映する
					x = robot.getX();
					y = robot.getY();
					//更新した後のロボットの座標における状態状態を取得する
					int after = judgeState(x, y);
					//選択した行動における報酬を報酬関数から得る
					int reward = judgeReward(x, y);
					/* Q 値を更新 */
					
					//デバック用：学習状況を分かりやすいように出力
					//System.out.println("s:" + s + " t:" + t);
					
					//時間差分方程式によってQＴａｂｌｅを更新する
					q1.update(state, action, after, reward);

					/* もし時間差分誤差が十分小さくなれば終了 */
					//ここでは更新後と更新前の差をとり誤差を調べている
					if (beforeQTable[state][action] - q1.getQTable(state, action) < 5)
						CountInSameQ++;
					
					//指定の回数だけ誤差が少なくなったら処理を終了する
					if (CountInSameQ == 3)
						break;
				}
			}
			// step 2: 学習したQテーブルの最適政策に基づいて
			// スタート位置からゴール位置まで移動
			/* ロボットを初期位置に戻す */
			robot.setX(mazeData.getSX());
			robot.setY(mazeData.getSY());
			// ゴール座標の取得
			int x = robot.getX();
			int y = robot.getY();
			//ロボットがゴールにつくまで処理を行う
			while (true) {
				// ロボットの位置座標を更新
				doAction(q1.selectAction(judgeState(x, y)), robot);
				x = robot.getX();
				y = robot.getY();
				// 現在の状態を描画する
				mazeView.repaint();
				// 速すぎるので 500msec 寝る
				Thread.sleep(500);
				// デバッグ用に現在位置を出力
				System.out.println("x = " + x + ", y = " + y);
				// もしゴールに到達すれば終了
				if (mazeData.get(x, y) == MazeData.GOAL) 
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	//報酬関数を定義
	private int judgeReward(int x, int y) {
		//現在の座標がブロック上であれば、今後選択しないようにマイナスの大きい値を与える
		if (mazeData.get(x, y) == MazeData.BLOCK)
			return -100;
		//現在の座標がゴール上であれば、ゴールへは最優先で向かってほしいため、非常に大きい正の値を与える
		if (mazeData.get(x, y) == MazeData.GOAL)
			return 10000;

		//ゴールの座標を取得
		int gx = mazeData.getGX();
		int gy = mazeData.getGY();

		//ゴールと現在位置のユークリッド距離から、ゴールに近ければ報酬は大きく、遠くなれば報酬は小さくなるような関数を設定
		//分子の３００は報酬の大きさを調整
		//分母の「＋１」は分母が０になることを防ぐため
		return 300 / ((int) Math.sqrt(Math.pow(gx - x, 2) + Math.pow(gy - y, 2)) + 1);
	}

	//選択された行動に応じたロボットの座標更新を行う
	private void doAction(int action, Robot robot) {
		//マップの外の座標に更新しないように設定
		if (action == 2 && robot.getX() + 1 <= width-1)
			robot.setX(robot.getX() + 1);
		if (action == 1 && robot.getY() + 1 <= height-1)
			robot.setY(robot.getY() + 1);
		if (action == 0 && robot.getY() - 1 >= 0)
			robot.setY(robot.getY() - 1);
		if (action == 3 && robot.getX() - 1 >= 0)
			robot.setX(robot.getX() - 1);
	}

	//マップ上の座標を一意の状態に対応付ける為の関数
	private int judgeState(int x, int y) {
		return 9 * y + x;
	}

	/**
	 * 描画用のビューを登録
	 */
	public void setView(MazeView view) {
		mazeView = view;
	}

	/**
	 * ロボットオブジェクトを取得する
	 * 
	 * @return ロボットオブジェクト
	 */
	public Robot getRobot() {
		return robot;
	}

	/**
	 * 迷路データオブジェクトを取得する
	 * 
	 * @return 迷路データオブジェクト
	 */
	public MazeData getMazeData() {
		return mazeData;
	}

	/** 迷路データ */
	private MazeData mazeData = null;
	/** ロボットデータ */
	private Robot robot = null;

	/** 描画用オブジェクト */
	private MazeView mazeView = null;
	//迷路データから得た細かい情報
	private int height;
	private int width;
	private int statesNumber;
	private int actionNumber;
}
