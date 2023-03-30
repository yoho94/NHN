package org.nhnacademy.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 채팅을 하는 유저 클래스.
 */
@SuppressWarnings("java:S106") // sys.out 검사 억제
public class User implements Runnable {

    private final String name;
    private String target;
    private final Socket socket;
    private final Logger logger = LogManager.getLogger(User.class);
    private final Set<String> ignoreSet;

    /**
     * Instantiates a new User.
     *
     * @param name   유저 이름(아이디).
     * @param socket the socket.
     */
    public User(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
        this.target = "@all";
        ignoreSet = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter sysOutput = new BufferedWriter(new OutputStreamWriter(System.out));
            BufferedReader sysInput = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // 서버에서 계속 읽기
            new Thread(() -> {
                try {
                    String line;
                    while (!Thread.currentThread().isInterrupted()) {
                        if ((line = reader.readLine()) == null) { // null 이면 종료
                            Thread.currentThread().interrupt();
                            break;
                        }

                        receive(line, sysOutput);
                    }

                } catch (IOException e) {
                    logger.error("User > run > Thread");
                    logger.error(e.getMessage());
                }
            }).start();

            // 계속 입력 받기
            String line;
            while (!Thread.currentThread().isInterrupted()) {
                if ((line = sysInput.readLine()) == null
                    || line.equals("!끝")) { // null, 끝 이면 종료
                    Thread.currentThread().interrupt();
                    break;
                }

                send(line, writer);
            }


        } catch (IOException e) {
            logger.error("User > run");
            logger.error(e.getMessage());
        } finally {
            Thread.currentThread().interrupt();
        }

    }

    private void send(String msg, BufferedWriter output) throws IOException {
        if (msg == null) {
            return;
        }

        if (msg.startsWith("!차단")) {
            ignoreSet.add(
                msg.substring(3).trim()
            );
            return;
        }

        if (msg.startsWith("@@")) {
            msg = msg.toLowerCase();

            output.write(msg);

            if (msg.startsWith("@@ connect")) {
                output.write(" ");
                output.write(getName());
            }

            output.newLine();
            output.flush();
            return;
        }

        String message = msg;
        if (msg.startsWith("@")) {
            // 타겟만 바꿈
            if (msg.trim().split(" ").length == 1) {
                setTarget(msg);
                return;
            }

            setTarget(msg.substring(0, msg.indexOf(' ')));
            message = msg.substring(getTarget().length() + 1);
        }

        output.write(getTarget() + " ");
        output.write(message);
        output.newLine();
        output.flush();
    }

    private void receive(String msg, BufferedWriter output) throws IOException {
        if (msg == null) {
            return;
        }

        // 비정상 메시지
        if (msg.trim().split(" ").length <= 1) {
            return;
        }

        String sender = msg.substring(1, msg.indexOf(' '));

        if (ignoreSet.contains(sender)) {
            return;
        }

        output.write(msg);
        output.newLine();
        output.flush();
    }

}
