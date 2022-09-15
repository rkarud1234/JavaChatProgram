import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMulti {

	public ServerMulti(int port, ServerGUI sg) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port); // 서버 소켓 할당
			// 클라이언트가 접속하기를 대기
			while (true) {
				Socket socket = serverSocket.accept(); // 클라이언트 소켓 할당
				Thread th = new PerClientThread(socket, sg); // 동시 채팅을 위해 클라이언트 당 스레드 할당
				th.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class PerClientThread extends Thread {
	static Vector<PrintWriter> list = new Vector<PrintWriter>(); // 클라이언트 관리를 위한 list(채팅내역)
	static Vector<String> users = new Vector<String>();

	Socket socket; // 클라이언트 소켓
	PrintWriter writer;
	ServerGUI sg;

	public PerClientThread(Socket socket, ServerGUI sg) {
		this.socket = socket;
		this.sg = sg;
		try {
			writer = new PrintWriter(socket.getOutputStream());
			list.add(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String name = null;
		InputStream is;
		try {
			// 처음 연결이 되었을 때
			is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);

			// 초기 입력받은 값으로 이름 할당
			name = reader.readLine();
			users.add(name);
			sendAll("#" + name + "님이 들어오셨습니다.");
			sendUserList();

			while (true) {
				// 연결되어있는동안 메시지를 전송할 수 있다.
				String str = reader.readLine();
				if (str == null)
					break;
				else {
					sendAll("[" + name + "] " + str);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			list.remove(writer);
			users.remove(name);
			sendAll("#" + name + "님이 나가셨습니다.");
			sendUserList();
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}

	private void sendUserList() {
		for (PrintWriter writer : list) {
			writer.println("!user");
			for (String user : users) {
				writer.println(user);
			}
			writer.flush();
		}
	}

	// broadcast
	private void sendAll(String str) {
		sg.appendRoom(str + "\n");
		for (PrintWriter writer : list) {
			writer.println(str);
			writer.flush();
		}
	}
}
