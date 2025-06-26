package emz;

import java.io.File;
import java.util.Scanner;

public class Main {

	// NOT SAFE
	private static File file = new File("../emzasm/bin/neki.bin");
	// private static File file = null;
	private static Emz1001 procesor;

	public static void main(String[] args) {
		if (file == null) {
			file = readCLIForFIle();
		}

		procesor = new Emz1001(file);
		if (procesor == null) {
			System.exit(1);
		}
		procesor.run(false);
		System.exit(0);

	}

	// method not secure
	// method returns null if error accurs
	static File readCLIForFIle() {
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter file path: ");
		String line = sc.nextLine();
		sc.close();
		line = line.trim();
		File f = new File(line);
		if (!line.endsWith(".bin")) {
			return null;
		}

		if (f.exists() && f.isFile()) {
			return f;
		}
		return null;

	}
}
