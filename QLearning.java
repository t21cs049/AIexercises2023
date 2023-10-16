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
	 * @param state   現在の状態
	 * @param epsilon ランダムに行動を選択する確率（0.0〜1.0）
	 * @return 選択された行動番号
	 */
	public int selectAction(int state, double epsilon) {
		int max = 0;
		for (int i = 1; i < qTable[state].length; i++) {
			if (qTable[state][i] > qTable[state][max]) {
				max = i;
			}
		}
		//100*epsilonで１〜１００の乱数と比べる。
		return max;
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
				+ ((reward + qTable[after][selectAction(after)] - qTable[before][action]));
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
