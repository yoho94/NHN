package org.nhnacademy.tree;

import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 레드블랙트리 자료구조를 사용하는 클래스.
 */
public class RedBlackTree {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    @SuppressWarnings("java:S106") // sys.out 검사 억제.
    public static void main(String[] args) {
        // 1. 정수를 저장하는 해당 클래스 인스턴스를 작성하세요.
        TreeMap<Integer, Integer> map = new TreeMap<>();
        TreeSet<Integer> set = new TreeSet<>();

        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            // 2. 객체에 30개의 무작위 정수를 추가하세요.
            int randomInt = random.nextInt(100);
            set.add(randomInt);
            map.put(randomInt, randomInt);

            // 3. 삽입된 무작위 정수들을 삽입 순서대로 출력하세요.
            System.out.println(randomInt);
        }

        // 4. 삽입된 무작위 정수들을 내림 차순으로 정렬해서 출력하세요.
        System.out.println("=== 4. MAP ===");
        for (int key : map.descendingKeySet()) {
            System.out.println(map.get(key));
        }

        System.out.println("=== 4. SET ===");
        for (int value : set.descendingSet()) {
            System.out.println(value);
        }

        // 5. 삽입된 무작위 정수들의 합을 구하는 메소드를 작성하고, 실행해서 출력하는 코드를 작성하세요.
        System.out.println("=== 5. MAP ===");
        System.out.println(sum(map.keySet()));

        System.out.println("=== 5. SET ===");
        System.out.println(sum(set));

    }

    // 5. 삽입된 무작위 정수들의 합을 구하는 메소드를 작성하고, 실행해서 출력하는 코드를 작성하세요.
    private static int sum(Iterable<Integer> iterable) {
        int sum = 0;

        for (int i : iterable) {
            sum += i;
        }

        return sum;
    }
}
