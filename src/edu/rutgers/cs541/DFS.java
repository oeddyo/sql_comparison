package edu.rutgers.cs541;

import java.util.Vector;

public class DFS {

	Vector<Integer> a;

	DFS() {
		a = new Vector<Integer>();
		a.add(0);
		a.add(1);
		a.add(2);
		a.add(4);
		// dfs(0, new Vector<Integer>());
		bfs();
	}

	private void bfs() {
		Vector<Vector<Integer>> q = new Vector<Vector<Integer>>();
		for (int i = 0; i < a.size(); i++) {
			Vector<Integer> v = new Vector<Integer>();
			v.add(i);
			q.add(v);
		}
		int h = 0;
		while (h < q.size()) {
			Vector<Integer> last = q.elementAt(h);
			int start = last.elementAt(last.size() - 1);
			for (int i = start + 1; i < a.size(); i++) {
				Vector<Integer> v = new Vector<Integer>();
				for (Integer j : q.elementAt(h)) {
					v.add(j);
				}
				v.add(i);
				q.add(v);
			}
			h++;
		}
		for (int j = 0; j < q.size(); j++) {
			Vector<Integer> res = q.elementAt(j);
			Debug.printVector(res);
		}
	}

	private void dfs(int start, Vector<Integer> res) {
		if (start == a.size()) {
			for (int i = 0; i < res.size(); i++) {
				System.out.print(res.elementAt(i));
				System.out.print(',');
			}
			System.out.println();
			return;
		}
		dfs(start + 1, res);
		res.add(a.elementAt(start));
		dfs(start + 1, res);
		res.remove(res.size() - 1);

	}

	public static void main(String[] args) {
		DFS d = new DFS();
	}

}
