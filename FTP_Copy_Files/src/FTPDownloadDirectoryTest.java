import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


public class FTPDownloadDirectoryTest {

	public static void main(String[] args) {
		String server = args[0];
		int port = 21;
		String user = args[1];
		String pass = args[2];
		String remoteDirPath=args[3];
		String currentdir= args[4];
		String saveDirPath=args[5];

		FTPClient ftpClient = new FTPClient();

		try {
			// connect and login to the server
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);

			// use local passive mode to pass firewall
			ftpClient.enterLocalPassiveMode();

			System.out.println("Connected");

			//remoteDirPath = "/16.11_Release/1611.01";
			//String saveDirPath = "D:/Century_Checkout/FTP";

			FTPDownloadDirectoryTest.downloadDirectory(ftpClient, remoteDirPath, currentdir, saveDirPath);

			// log out and disconnect from the server
			ftpClient.logout();
			ftpClient.disconnect();

			System.out.println("Disconnected");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Download a whole directory from a FTP server.
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param parentDir Path of the parent directory of the current directory being
	 * downloaded.
	 * @param currentDir Path of the current directory being downloaded.
	 * @param saveDir path of directory where the whole remote directory will be
	 * downloaded and saved.
	 * @throws IOException if any network or IO error occurred.
	 */
	public static void downloadDirectory(FTPClient ftpClient, String parentDir,
			String currentDir, String saveDir) throws IOException {
		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir;
		}

		FTPFile[] subFiles = ftpClient.listFiles(dirToList);

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = parentDir + "/" + currentDir + "/"
						+ currentFileName;
				if (currentDir.equals("")) {
					filePath = parentDir + "/" + currentFileName;
				}

				String newDirPath = saveDir + parentDir + File.separator
						+ currentDir + File.separator + currentFileName;
				if (currentDir.equals("")) {
					newDirPath = saveDir + parentDir + File.separator
							  + currentFileName;
				}

				if (aFile.isDirectory()) {
					// create the directory in saveDir
					File newDir = new File(newDirPath);
					boolean created = newDir.mkdirs();
					if (created) {
						System.out.println("CREATED the directory: " + newDirPath);
					} else {
						System.out.println("COULD NOT create the directory: " + newDirPath);
					}

					// download the sub directory
					downloadDirectory(ftpClient, dirToList, currentFileName,
							saveDir);
				} else {
					// download the file
					boolean success = downloadSingleFile(ftpClient, filePath,
							newDirPath);
					if (success) {
						System.out.println("DOWNLOADED the file: " + filePath);
					} else {
						System.out.println("COULD NOT download the file: "
								+ filePath);
					}
				}
			}
		}
	}
	
	/**
	 * Download a single file from the FTP server
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteFilePath path of the file on the server
	 * @param savePath path of directory where the file will be stored
	 * @return true if the file was downloaded successfully, false otherwise
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean downloadSingleFile(FTPClient ftpClient,
			String remoteFilePath, String savePath) throws IOException {
		File downloadFile = new File(savePath);

		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}

		OutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(downloadFile));
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			return ftpClient.retrieveFile(remoteFilePath, outputStream);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}



}
