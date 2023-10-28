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
		height = mazeData.getHeight();
		width = mazeData.getWidth();
		statesNumber = height*width;
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
				int beforeQTable[][] = new int[statesNumber][actionNumber];
				for (int i = 0; i < beforeQTable.length; i++) {
					for (int j = 0; j < beforeQTable[i].length; j++) {
						beforeQTable[i][j] = 10000;
					}
				}
				int CountInSameQ = 0;
				for (int s = 0; s < steps; s++) { // ステップ数だけ繰り返し
					/* ε-Greedy 法により行動を選択 */
					// ロボットの現在位置を取得
					int x = robot.getX();
					int y = robot.getY();
					int state = judgeState(x, y);
					double epsilon = 0.5;
					int action = q1.selectAction(state, epsilon);
					/* 選択した行動を実行 (ロボットを移動する) */
					judgeAction(action, robot);
					/* 新しい状態を観測＆報酬を得る */
					x = robot.getX();
					y = robot.getY();
					int after = judgeState(x, y);
					int reward = judgeReward(x, y);
					/* Q 値を更新 */
					System.out.println("s:" + s + " t:" + t);
					q1.update(state, action, after, reward);

					/* もし時間差分誤差が十分小さくなれば終了 */
					if (beforeQTable[state][action] - q1.getQTable(state, action) < 5)
						CountInSameQ++;

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
			while (true) {
				// ロボットの位置座標を更新
				judgeAction(q1.selectAction(judgeState(x, y)), robot);
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

	private int judgeReward(int x, int y) {

		if (mazeData.get(x, y) == MazeData.BLOCK)
			return -100;

		if (mazeData.get(x, y) == MazeData.GOAL)
			return 10000;

		int gx = mazeData.getGX();
		int gy = mazeData.getGY();

		return 300 / ((int) Math.sqrt(Math.pow(gx - x, 2) + Math.pow(gy - y, 2)) + 1);
	}

	private void judgeAction(int action, Robot robot) {
		if (action == 2 && robot.getX() + 1 <= width-1)
			robot.setX(robot.getX() + 1);
		if (action == 1 && robot.getY() + 1 <= height-1)
			robot.setY(robot.getY() + 1);
		if (action == 0 && robot.getY() - 1 >= 0)
			robot.setY(robot.getY() - 1);
		if (action == 3 && robot.getX() - 1 >= 0)
			robot.setX(robot.getX() - 1);
	}

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
	private int height;
	private int width;
	private int statesNumber;
	private int actionNumber;
}
