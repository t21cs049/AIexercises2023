
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
	public int selectAction(int state, double epsilon) {
		int action;
		Random rand = new Random();
		//最初に設定する借りの最大値はランダムに設定する
		//これにより値が等しいQ値が複数あったとしても、特定の方向だけ選択するというような事を防いでいる。
		int max = rand.nextInt(4);
		//maxはQ値が最大である行動を示している
		//行動４つに対するQ値の中から最大値を見つける
		for (int i = 1; i < qTable[state].length; i++) {
			if (qTable[state][i] >= qTable[state][max]) {
				max = i;
			}
		}
		// 100*epsilonで１〜１００の乱数と比べる。
		int num = rand.nextInt(100);
		if (num < 100 * epsilon)
			action = max;
		else {
			//一定の確率でランダムに行動を選択する
			int randAction = rand.nextInt(4);
			action = randAction;
		}
		//行動を表す番号を返す
		return action;
	}

	/**
	 * Greedy 法により行動を選択する
	 * 
	 * @param state 現在の状態
	 * @return 選択された行動番号
	 */
	//最終的なｑＴａｂｌｅの値を参照して行動を純粋に選択するためのプログラム
	//最大値を例外なく選択するように作成
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
		//時間差分方程式を計算する
		qTable[before][action] = qTable[before][action]
				+ (alpha * (reward + gamma * qTable[after][selectAction(after)] - qTable[before][action]));
//デバック用コード　ｑＴｂａｌｅの内容をここで表示する。
//デバック時以外は処理速度を遅くしてしまうので、非表示にする。
//		System.out.println("/////////////////////////");
//		for (int i = 0; i < qTable.length; i++) {
//			System.out.print("S " + i + " : ");
//			for (int j = 0; j < qTable[i].length; j++) {
//				System.out.print(qTable[i][j] + " ");
//			}
//			System.out.println();
//		}
	}

	// フィールド
	private double qTable[][] = null;
	private double alpha = 0;
	private double gamma = 0;

	//指定されたQTableの値を返す
	public double getQTable(int state, int action) {
		return qTable[state][action];
	}
}
