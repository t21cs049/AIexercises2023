import java.io.*;

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
		// ロボットを生成
		robot = new Robot(mazeData.getSX(), mazeData.getSY());
	}

	/**
	 * 実行用関数
	 */
	public void run() {
		try {
			// step 1: Q学習する
			QLearning q1 = new QLearning(16, 4, 1, 1);

			int trials = 500; // 強化学習の試行回数
			int steps = 100; // １試行あたりの最大ステップ数
			for (int t = 1; t <= trials; t++) { // 試行回数だけ繰り返し
				/* ロボットを初期位置に戻す */
				robot.setX(mazeData.getSX());
				robot.setY(mazeData.getSY());
				for (int s = 0; s < steps; s++) { // ステップ数だけ繰り返し
					/* ε-Greedy 法により行動を選択 */
					// ロボットの現在位置を取得
					int x = robot.getX();
					int y = robot.getY();

					int state = judgeState(x, y);
					double epsilon = 0.5;
					int action = q1.selectAction(state, epsilon, mazeData, x, y);
					/* 選択した行動を実行 (ロボットを移動する) */
					judgeAction(action, robot);
					/* 新しい状態を観測＆報酬を得る */
					int after = judgeState(x, y);
					int reward = judgeReward(x, y);
					/* Q 値を更新 */
					q1.update(state, action, after, reward);
					/* もし時間差分誤差が十分小さくなれば終了 */
					if (reward == q1.getQTable(after, action))
						break;
				}
			}
			// step 2: 学習したQテーブルの最適政策に基づいて
			// スタート位置からゴール位置まで移動
			/* ロボットを初期位置に戻す */
			robot.setX(mazeData.getSX());
			robot.setY(mazeData.getSY());
			// ゴール座標の取得
			int gx = mazeData.getGX();
			int gy = mazeData.getGY();
			while (true) {
				int x = robot.getX();
				int y = robot.getY();
				// ロボットの位置座標を更新
				judgeAction(q1.selectAction(judgeState(x, y)), robot);
				// 現在の状態を描画する
				mazeView.repaint();
				// 速すぎるので 500msec 寝る
				Thread.sleep(500);
				// もしゴールに到達すれば終了
				if (x == gx && y == gy)
					break;
				// デバッグ用に現在位置を出力
//					System.out.println("x = " + x + ", y = " + y);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private int judgeReward(int x, int y) {
		int up = 0, down = 0, right = 0, left = 0;
		if (mazeData.get(x, y - 1) == MazeData.BLOCK)
			up = 1;
		if (mazeData.get(x, y + 1) == MazeData.BLOCK)
			down = 1;
		if (mazeData.get(x + 1, y) == MazeData.BLOCK)
			right = 1;
		if (mazeData.get(x - 1, y) == MazeData.BLOCK)
			left = 1;

		if (mazeData.get(x, y) == MazeData.BLOCK)
			return -10;
		
		int gx = mazeData.getGX();
		int gy = mazeData.getGY();
		
//		int num;
		if (right == 1 && down == 1)
			return -1;
		if (right + left + down + up >= 3)
			return -5;
		
		return 10 / ((int) Math.sqrt(Math.pow(gx-x,2)+Math.pow(gy-y,2)) + 1);

		
//			if (right == 1 && down == 1)
//				return 2;
//		if (right + left + down + up >= 3)
//			return 1;
//
//		return 3;

	}

	private void judgeAction(int action, Robot robot) {
		if (action == 2)
			robot.setX(robot.getX() + 1);
		if (action == 1)
			robot.setY(robot.getY() + 1);
		if (action == 0)
			robot.setY(robot.getY() - 1);
		if (action == 3)
			robot.setX(robot.getX() - 1);
	}

	private int judgeState(int x, int y) {
		int up = 0, down = 0, right = 0, left = 0;
		if (mazeData.get(x, y - 1) == MazeData.BLOCK)
			up = 1;
		if (mazeData.get(x, y + 1) == MazeData.BLOCK)
			down = 1;
		if (mazeData.get(x + 1, y) == MazeData.BLOCK)
			right = 1;
		if (mazeData.get(x - 1, y) == MazeData.BLOCK)
			left = 1;

		return 8 * up + 4 * down + 2 * right + 1 * left;
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
}
