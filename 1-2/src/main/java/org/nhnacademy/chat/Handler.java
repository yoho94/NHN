package org.nhnacademy.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 채팅 서버의 핸들러.
 */
public class Handler implements Runnable {

    private final Socket socket;
    private final Logger logger = LogManager.getLogger(Handler.class);
    private static final List<Handler> handlers = new LinkedList<>();
    private BufferedWriter writer;
    private String userId;
    private final LocalDateTime accessTime;
    protected static final Set<String> blackList = new HashSet<>();

    /**
     * Instantiates a new Handler.
     *
     * @param socket the socket
     */
    public Handler(Socket socket) {
        this.socket = socket;
        this.userId = "";
        accessTime = LocalDateTime.now();
    }


    @Override
    public void run() {
        handlers.add(this);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (!Thread.currentThread().isInterrupted()) {
                String msg;
                if ((msg = reader.readLine()) == null) {
                    break;
                }

                logger.debug(msg);

                messageProcess(msg);
            }
        } catch (IOException e) {
            logger.debug("Handler > run");
            logger.debug(e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger.debug("Handler > run > finally");
                logger.debug(e.getMessage());
            }
            synchronized (handlers) {
                handlers.remove(this);
            }
            Thread.currentThread().interrupt();
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    private void messageProcess(String msg) throws IOException {
        // 비정상 메시지
        if (msg.trim().split(" ").length == 1) {
            return;
        }

        // ID 설정 안된 클라이언트 무시
        if (userId.isBlank() && !msg.toLowerCase().startsWith("@@ connect")) {
            return;
        }

        if (msg.startsWith("@@")) {
            String[] cmd = msg.split(" ");

            commandProcess(cmd);
            return;
        }

        String[] msgArray = msg.split(" ");

        // 비정상 메시지
        if (msgArray.length <= 1) {
            return;
        }

        String target = msgArray[0];

        // 비정상 메시지
        if (!target.startsWith("@")) {
            return;
        }

        msg = msg.substring(target.length() + 1);

        if (target.equalsIgnoreCase("@all")) {
            broadcast(msg);
            return;
        }

        directMessage(target.substring(1), msg);

    }

    @SuppressWarnings("java:S3776") // 복잡도 검사 억제
    private void commandProcess(String[] cmdArray) throws IOException {
        // 잘못된 명령
        if (cmdArray.length < 2) {
            return;
        }

        String cmd = cmdArray[1];

        if (cmd.equalsIgnoreCase("connect")) {
            String userName = cmdArray[2];

            // 블랙리스트
            synchronized (blackList) {
                if (blackList.contains(userName)) {
                    writer.write("#@ connect 접속 불가 사용자입니다.");
                    writer.newLine();
                    writer.flush();
                    return;
                }
            }

            // 이미 접속 중인 사용자 확인.
            synchronized (handlers) {
                for (Handler handler : handlers) {
                    if (handler != this && handler.getUserId().equals(userName)) {
                        writer.write("#@ connect 접속 실패. 이미 같은 이름의 사용자가 있습니다.");
                        writer.newLine();
                        writer.flush();
                        return;
                    }
                }
            }

            writer.write("#@ connect 성공.");
            setUserId(userName);

        } else if (cmd.equalsIgnoreCase("userList")) {
            synchronized (handlers) {
                for (Handler handler : handlers) {
                    if (!handler.getUserId().isBlank()) {
                        writer.write("#@ userList " + handler.getUserId());
                        writer.newLine();
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("time")) {
            writer.write("#@ time " + LocalDateTime.now().toString());
        } else if (cmd.equalsIgnoreCase("accessTime")) {
            writer.write("#@ accessTime " + accessTime.toString());
        }

        writer.newLine();
        writer.flush();
    }

    private BufferedWriter getOutput() {
        return writer;
    }

    private void broadcast(String msg) throws IOException {
        synchronized (handlers) {
            for (Handler handler : handlers) {
                handler.getOutput().write("#");
                handler.getOutput().write(getUserId());
                handler.getOutput().write(" ");
                handler.getOutput().write(msg);
                handler.getOutput().newLine();
                handler.getOutput().flush();
            }
        }
    }

    private void directMessage(String userId, String msg) throws IOException {
        synchronized (handlers) {
            for (Handler handler : handlers) {
                if (handler.getUserId().equals(userId)) {
                    handler.getOutput().write("#");
                    handler.getOutput().write(getUserId());
                    handler.getOutput().write(" ");
                    handler.getOutput().write(msg);
                    handler.getOutput().newLine();
                    handler.getOutput().flush();
                    break;
                }
            }
        }
    }

}
