package marina_CKY;

import java.util.*;
import java.io.*;

public class PennTreebankReader {

	static class TreeCollection extends AbstractCollection<MarinaTree<String>> {

		List<File> files;

		static class TreeIteratorIterator implements Iterator<Iterator<MarinaTree<String>>> {
			Iterator<File> fileIterator;
			Iterator<MarinaTree<String>> nextTreeIterator;

			public boolean hasNext() {
				return nextTreeIterator != null;
			}

			public Iterator<MarinaTree<String>> next() {
				Iterator<MarinaTree<String>> currentTreeIterator = nextTreeIterator;
				advance();
				return currentTreeIterator;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			private void advance() {
				nextTreeIterator = null;
				while (nextTreeIterator == null && fileIterator.hasNext()) {
					try {
						File file = fileIterator.next();
						nextTreeIterator = new Trees.PennTreeReader(new BufferedReader(new FileReader(file)));
					} catch (FileNotFoundException e) {
					}
				}
			}

			TreeIteratorIterator(List<File> files) {
				this.fileIterator = files.iterator();
				advance();
			}
		}

		public Iterator<MarinaTree<String>> iterator() {
			return new ConcatenationIterator<MarinaTree<String>>(new TreeIteratorIterator(files));
		}

		public int size() {
			int size = 0;
			Iterator i = iterator();
			while (i.hasNext()) {
				size++;
				i.next();
			}
			return size;
		}

		private List<File> getFilesUnder(String path, FileFilter fileFilter) {
			File root = new File(path);
			List<File> files = new ArrayList<File>();
			addFilesUnder(root, files, fileFilter);
			return files;
		}

		private void addFilesUnder(File root, List<File> files, FileFilter fileFilter) {
			//if (!fileFilter.accept(root))
			//	return;
			if (root.isFile()) {
				files.add(root);
				return;
			}
			if (root.isDirectory()) {
				File[] children = root.listFiles();
				for (int i = 0; i < children.length; i++) {
					File child = children[i];
					addFilesUnder(child, files, fileFilter);
				}
			}
		}

		public TreeCollection(String path, int lowFileNum, int highFileNum) {
			FileFilter fileFilter = new NumberRangeFileFilter(".mrg", lowFileNum, highFileNum, true);
			this.files = getFilesUnder(path, fileFilter);
		}
	}

	public static Collection<MarinaTree<String>> readTrees(String path) {
		return readTrees(path, -1, Integer.MAX_VALUE);
	}

	public static Collection<MarinaTree<String>> readTrees(String path, int lowFileNum, int highFileNumber) {
		return new TreeCollection(path, lowFileNum, highFileNumber);
	}

	public static void main(String[] args) {
		
		}
	}


