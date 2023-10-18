
/**
 * Ｑ学習を行うクラス
 */
import java.util.Random;

public class QLearning {

	/**
	 * Ｑ学習を行うオブジェクトを生成する
	 * 
	 * @param states  状態数
	 * @param actions 行動数
	 * @param alpha   学習率（0.0〜1.0）
	 * @param gamma   割引率（0.0〜1.0）
	 */
	public QLearning(int states, int actions, double alpha, double gamma) {
		this.qTable = new double[states][actions];
		this.states = states;
		this.alpha = alpha;
		this.gamma = gamma;
	}

	/**
	 * epsilon-Greedy 法により行動を選択する
	 * 
	 * @param state    現在の状態
	 * @param epsilon  ランダムに行動を選択する確率（0.0〜1.0）
	 * @param mazeData
	 * @param y
	 * @param x
	 * @return 選択された行動番号
	 */
	public int selectAction(int state, double epsilon, MazeData mazeData, int x, int y) {
////治す
		// 100 3 2 1
		int max = 0;
		boolean notBlockFlag = false;
		int action = 2;
		Random rand = new Random();
		int k;
		//while (!notBlockFlag) {
		for (k = 0; k < 4; k++) {
			if (judgeBlockByAction(k, mazeData, x, y)) {
				break;
				//notBlockFlag = true;
				//max = i;
			}
		}
		max = k; 
		//}
		for (int i = 1; i < qTable[state].length; i++) {
			if (qTable[state][i] >= qTable[state][max]) {
				if (judgeBlockByAction(i, mazeData, x, y))
					max = i;
			}
		}
		// 100*epsilonで１〜１００の乱数と比べる。
		int num = rand.nextInt(100);
		if (num < 100 * epsilon)
			action = max;
		else {
			while (!notBlockFlag) {
				int randAction = rand.nextInt(4);
				action = randAction;
				if (judgeBlockByAction(action, mazeData, x, y))
					notBlockFlag = true;
			}
		}
		return action;
	}

//	public int selectAction(int state, double epsilon) {
//
//		int max = 0;
//		for (int i = 1; i < qTable[state].length; i++) {
//			if (qTable[state][i] >= qTable[state][max]) {
//					max = i;
//			}
//		}
//		// 100*epsilonで１〜１００の乱数と比べる。
//		Random rand = new Random();
//		int num = rand.nextInt(100);
//
//		if (num < 100 * epsilon)
//			return max;
//		else {
//				int randAction = rand.nextInt(4);
//				return randAction;
//		}
//	}

	private boolean judgeBlockByAction(int action, MazeData mazeData, int x, int y) {
		// System.out.println("x:" + x + "y:" + y +"action" + action);
		if (action == 2)
			if (mazeData.get(x + 1, y) != MazeData.BLOCK)
				return true;
		if (action == 1)
			if (mazeData.get(x, y + 1) != MazeData.BLOCK)
				return true;
		if (action == 0)
			if (mazeData.get(x, y - 1) != MazeData.BLOCK)
				return true;
		if (action == 3)
			if (mazeData.get(x - 1, y) != MazeData.BLOCK)
				return true;

		return false;
	}

	/**
	 * Greedy 法により行動を選択する
	 * 
	 * @param state 現在の状態
	 * @return 選択された行動番号
	 */
	public int selectAction(int state) {
		int max = 0;
		for (int i = 1; i < qTable[state].length; i++) {
			if (qTable[state][i] > qTable[state][max]) {
				max = i;
			}
		}
		return max;
	}

	/**
	 * Ｑ値を更新する
	 * 
	 * @param before 状態
	 * @param action 行動
	 * @param after  遷移後の状態
	 * @param reward 報酬
	 */
	public void update(int before, int action, int after, double reward) {
		qTable[before][action] = qTable[before][action]
				+ (alpha * (reward + gamma * qTable[after][selectAction(after)] - qTable[before][action]));

		System.out.println("/////////////////////////");
		for (int i = 0; i < qTable.length; i++) {
			for (int j = 0; j < qTable[i].length; j++) {
				System.out.print(qTable[i][j] + " ");
			}
			System.out.println();
		}
	}

	// フィールド
	private double qTable[][] = null;
	private double alpha = 0;
	private double gamma = 0;
	private int states;

	public int getQTable(int after, int action) {
		return (int) qTable[after][action];
	}
}
