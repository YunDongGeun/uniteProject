package uniteProject.mvc.service;

import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkedDataHandler {
    private Map<String, ByteArrayOutputStream> dataBuffers = new ConcurrentHashMap<>();
    private Map<String, Long> lastUpdateTime = new ConcurrentHashMap<>();
    @Getter
    private String currentStudentNumber;
    private static final long TIMEOUT_MS = 30000;
    private static final int MAX_PACKET_SIZE = 1024;

    public void setCurrentStudentNumber(String studentNumber) {
        this.currentStudentNumber = studentNumber;
        // 기존 버퍼가 있다면 정리
        if (dataBuffers.containsKey(studentNumber)) {
            cleanup(studentNumber);
        }
        // 새 버퍼 초기화
        dataBuffers.put(studentNumber, new ByteArrayOutputStream());
        lastUpdateTime.put(studentNumber, System.currentTimeMillis());
    }

    public byte[] handleChunkedData(byte[] chunk) {
        if (currentStudentNumber == null) {
            throw new IllegalStateException("처리 중인 학생 정보가 없습니다.");
        }

        System.out.println("buffer 초기화 전.");
        ByteArrayOutputStream buffer = dataBuffers.get(currentStudentNumber);
        if (buffer == null) {
            throw new IllegalStateException("버퍼가 초기화되지 않았습니다.");
        }

        synchronized (buffer) {
            System.out.println("sync 진입");
            try {
                System.out.println(chunk.length + " " + Arrays.toString(chunk));
                buffer.write(chunk);
                lastUpdateTime.put(currentStudentNumber, System.currentTimeMillis());

                System.out.println(System.currentTimeMillis());
                if (chunk.length < MAX_PACKET_SIZE) {
                    System.out.println("??");
                    byte[] completeData = buffer.toByteArray();
                    cleanup(currentStudentNumber);
                    currentStudentNumber = null; // 처리 완료 후 학번 초기화
                    return completeData;
                }
                System.out.println("null 반환");
                return null;
            } catch (IOException e) {
                cleanup(currentStudentNumber);
                currentStudentNumber = null;
                throw new RuntimeException("청크 처리 중 오류 발생", e);
            }
        }
    }

    private void cleanup(String key) {
        dataBuffers.remove(key);
        lastUpdateTime.remove(key);
    }

    public void cleanupTimedOutBuffers() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = lastUpdateTime.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > TIMEOUT_MS) {
                cleanup(entry.getKey());
                if (entry.getKey().equals(currentStudentNumber)) {
                    currentStudentNumber = null;
                }
            }
        }
    }
}