import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class tsp {
	static final String OUTPUT_DIVIDER = "-----------------------------------------------\n";
	ArrayList<ArrayList<Cell>> maze;
	ArrayList<Cell> goals;
	Float[][] AdjacencyMat;

	class Cell {
		private char cellState;
		private int row;
		private int col;

		public int getRow() {
			return row;
		}

		public int getCol() {
			return col;
		}

		public char getValue() {
			return cellState;
		}

		public Cell(char cellState, int Row, int Col) {
			this.row = Row;
			this.col = Col;
			this.cellState = cellState;
		}

		public boolean isNotBlocked() {
			if (cellState == '*') {
				return false;
			} else {
				return true;
			}
		}

		public boolean isGoal(char G) {
			if (G == cellState) {
				return true;
			} else {
				return false;
			}
		}
	}

	class State implements Comparable<State> {
		State parent;
		float g, h;
		Cell c;

		public State(Cell cell, State parent, float g, float h) {
			this.parent = parent;
			this.g = g;
			this.h = h;
			this.c = cell;
		}

		@Override
		public int compareTo(State o) {
			float f1 = this.g + this.h;
			float f2 = o.g + o.h;
			if (f1 < f2) {
				return -1;
			} else if (f1 > f2) {
				return 1;
			} else {
				return breakTies(o);
			}
		}

		protected int breakTies(State o) {
			if (this.c.getRow() < o.c.getRow()) {
				return -1;
			} else if (this.c.getRow() > o.c.getRow()) {
				return 1;
			} else {
				if (this.c.getCol() < o.c.getCol()) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	}

	public tsp() {
		maze = new ArrayList<ArrayList<Cell>>();
		goals = new ArrayList<Cell>();
	}

	public Cell getTopCell(Cell cur) {
		int x = cur.getRow() - 1;
		int y = cur.getCol();
		if (x >= 0 && y >= 0 && x < maze.size() && y < maze.get(x).size())
			return maze.get(x).get(y);
		else
			return null;
	}

	public Cell getBottomCell(Cell cur) {
		int x = cur.getRow() + 1;
		int y = cur.getCol();
		if (x >= 0 && y >= 0 && x < maze.size() && y < maze.get(x).size())
			return maze.get(x).get(y);
		else
			return null;
	}

	public Cell getRightCell(Cell cur) {
		int x = cur.getRow();
		int y = cur.getCol() + 1;
		if (x >= 0 && y >= 0 && x < maze.size() && y < maze.get(x).size())
			return maze.get(x).get(y);
		else
			return null;
	}

	public Cell getLeftCell(Cell cur) {
		int x = cur.getRow();
		int y = cur.getCol() - 1;
		if (x >= 0 && y >= 0 && x < maze.size() && y < maze.get(x).size())
			return maze.get(x).get(y);
		else
			return null;
	}

	public void loadMaze(String input_file) {
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(input_file));

			int rowNumber = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				ArrayList<Cell> row = new ArrayList<Cell>();
				for (int i = 0; i < sCurrentLine.length(); i++) {
					Cell cell = new Cell(sCurrentLine.charAt(i), rowNumber, i);
					if (sCurrentLine.charAt(i) != ' '
							&& sCurrentLine.charAt(i) != '*') {
						goals.add(cell);
					}
					row.add(cell);
				}
				maze.add(row);
				rowNumber++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	public void doSearch(String input_file, int task, String output_log,
			String output_path) {
		loadMaze(input_file);
		if (task == 1)
			findAllShortestPaths(true, output_log, output_path);
		else
			findAllShortestPaths(false, null, null);

	}

	private void findAllShortestPaths(boolean log, String output_log,
			String output_path) {
		BufferedWriter pathBW = null;
		BufferedWriter logBW = null;
		if (log) {
			File pathFile = new File(output_path);
			File logFile = new File(output_log);
			// if file doesnt exists, then create it
			try {
				if (!pathFile.exists()) {
					pathFile.createNewFile();
				}
				if (!logFile.exists()) {
					logFile.createNewFile();
				}
				FileWriter logFW = new FileWriter(logFile.getAbsoluteFile());
				logBW = new BufferedWriter(logFW);
				FileWriter pathFW = new FileWriter(pathFile);
				pathBW = new BufferedWriter(pathFW);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Collections.sort(goals, new Comparator<Cell>() {
			public int compare(Cell o1, Cell o2) {
				if (o1.getValue() < o2.getValue())
					return -1;
				else
					return 1;
			}
		});
		// A HUGE ASSUMPTION FROM HERE ON. A is at index 0, because goals is
		// sorted
		AdjacencyMat = new Float[goals.size()][goals.size()];
		for (int i = 0; i < goals.size(); i++) {
			// The diagonal is of no use
			AdjacencyMat[i][i] = null;
			for (int j = i + 1; j < goals.size(); j++) {
				Cell start = goals.get(i), end = goals.get(j);

				float weight = findShortestPath(log, start, end, logBW);
				AdjacencyMat[i][j] = new Float(weight);
				AdjacencyMat[j][i] = new Float(weight);
			}
		}

		// log
		System.out.println("The resulting graph");
		System.out.print(OUTPUT_DIVIDER);
		for (int i = 0; i < goals.size(); i++) {
			for (int j = i + 1; j < goals.size(); j++) {
				Cell start = goals.get(i), end = goals.get(j);
				System.out.printf("%c,%c,%.1f\n", start.getValue(), end.getValue(),
						AdjacencyMat[i][j]);
				if (log) {
					try {
						pathBW.write(String.format("%c,%c,%.1f\n",
								start.getValue(), end.getValue(),
								AdjacencyMat[i][j]));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (log) {
			try {
				pathBW.close();
				logBW.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private float findShortestPath(boolean log, Cell start, Cell end,
			BufferedWriter logBW) {
		HashMap<Cell, State> closed = new HashMap<tsp.Cell, tsp.State>();
		PriorityQueue<State> queue = new PriorityQueue<tsp.State>();

		queue.add(new State(start, null, 0, calManhattenDis(start, end)));

		// -----------------------------------------------------
		System.out.printf("from '%c' to '%c'\n", start.getValue(), end.getValue());
		System.out.print(OUTPUT_DIVIDER);
		System.out.print("x,y,g,h,f\n");
		if (log) {
			try {
				logBW.write(String.format("from '%c' to '%c'\n", start.getValue(),
						end.getValue()));
				logBW.write(OUTPUT_DIVIDER);
				logBW.write("x,y,g,h,f\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// -----------------------------------------------------

		while (!queue.isEmpty()) {
			State current = queue.poll();
			// if already in the closed list then continue
			if (closed.containsKey(current.c)) {
				continue;
			}

			// -----------------------------------------------------
			System.out.printf("%d,%d,%.1f,%.1f,%.1f\n", current.c.col,
					current.c.row, current.g, current.h, current.h + current.g);
			if (log) {
				try {
					logBW.write(String.format("%d,%d,%.1f,%.1f,%.1f\n",
							current.c.col, current.c.row, current.g, current.h,
							current.h + current.g));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (current.c == end) {
				// Path info
				// ArrayList<State> path = new ArrayList<tsp.State>(
				// (int) current.g);
				// while (current.parent != null) {
				// path.add(current);
				// current = current.parent;
				// }
				// System.out.printf("from %c to %c\n", start.getValue(),
				// end.getValue());
				// System.out.print(OUTPUT_DIVIDER);
				// System.out.print("x,y,g,h,f\n");
				//
				// for (State n : path) {
				// System.out.printf("%d,%d,%.1f,%.1f,%.1f\n", n.c.col,
				// n.c.row, n.g, n.h, n.h + n.g);
				// }
				if (log) {
					try {
						logBW.write(OUTPUT_DIVIDER);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.print(OUTPUT_DIVIDER);
				return current.g;
			}

			Cell top = getTopCell(current.c);
			if (top != null && top.isNotBlocked()) {
				State topState = new State(top, current, current.g + 1,
						calManhattenDis(top, end));
				queue.add(topState);
			}
			Cell right = getRightCell(current.c);
			if (right != null && right.isNotBlocked()) {
				State rightState = new State(right, current, current.g + 1,
						calManhattenDis(right, end));
				queue.add(rightState);
			}
			Cell bottom = getBottomCell(current.c);
			if (bottom != null && bottom.isNotBlocked()) {
				State bottomState = new State(bottom, current, current.g + 1,
						calManhattenDis(bottom, end));
				queue.add(bottomState);
			}
			Cell left = getLeftCell(current.c);
			if (left != null && left.isNotBlocked()) {
				State leftState = new State(left, current, current.g + 1,
						calManhattenDis(left, end));
				queue.add(leftState);
			}

			closed.put(current.c, current);
		}
		System.err
				.println("Explored the whole graph, but cannot reach the End goal, "
						+ start.getValue() + " to " + end.getValue());
		System.exit(-1);
		return 0;
	}

	private int calManhattenDis(Cell start, Cell end) {
		return Math.abs(start.getRow() - end.getRow())
				+ Math.abs(start.getCol() - end.getCol());
	}

	public static void main(String[] args) {
		String task = null, input_file = null, output_path = null, output_log = null;
		for (int i = 0; i < args.length; i++) {
			if (i + 1 == args.length) {
				System.out.println("Invalid input: Missing arguments");
				System.exit(-1);
			} else {
				if (args[i].compareTo("-t") == 0) {
					task = args[++i];
				} else if (args[i].compareTo("-i") == 0) {
					input_file = args[++i];
				} else if (args[i].compareTo("-op") == 0) {
					output_path = args[++i];
				} else if (args[i].compareTo("-ol") == 0) {
					output_log = args[++i];
				} else {
					System.out.println("Invalid input: Wrong arguments");
					System.exit(-1);
				}
			}
		}

		if (task == null) {
			System.out.println("Invalid input: Missing arguments");
			System.exit(-1);
		}

		if (input_file == null) {
			System.out.println("Invalid input: Missing arguments");
			System.exit(-1);
		}
		if (output_path == null) {
			System.out.println("Invalid input: Missing arguments");
			System.exit(-1);
		}
		if (output_log == null) {
			System.out.println("Invalid input: Missing arguments");
			System.exit(-1);
		}

		tsp s = new tsp();
		s.doSearch(input_file, Integer.parseInt(task), output_log, output_path);

	}

}
