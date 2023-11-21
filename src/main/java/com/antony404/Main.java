package com.antony404;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    /**
     * 判断两个 case 是否是合理转换，并判断坐船后船的位置
     */
    private static Integer getBoatPos(Integer[] case1, Integer[] case2) {
        // 无效转换
        if (Arrays.equals(case1, case2)) {
            return -1;
        }

        List<Integer> diffPosList = getDiffPosList(case1, case2);

        // 船上每次只能坐两个人
        if (diffPosList.size() > 2) {
            return null;
        }

        // 不能出现相反的 01 转换
        if (diffPosList.size() == 2
                && !Objects.equals(case1[diffPosList.get(0)], case1[diffPosList.get(1)])) {
            return null;
        }

        // 魅魔
        if (diffPosList.contains(0)
                && Stream.of(1, 2, 4).anyMatch(diffPosList::contains)) {
            return null;
        }

        // 鬼父
        if (diffPosList.contains(1)
                && diffPosList.contains(3)) {
            return null;
        }

        // 骨科
        if (diffPosList.contains(2)
                && diffPosList.contains(3)) {
            return null;
        }

        // 基佬
        if (diffPosList.contains(2)
                && diffPosList.contains(4)) {
            return null;
        }

        if (case1[diffPosList.get(0)] == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 获取两个 case 的差异位置
     */
    private static List<Integer> getDiffPosList(Integer[] case1, Integer[] case2) {
        List<Integer> diffPosList = new ArrayList<>();
        for (int i = 0; i < case1.length; i++) {
            if (!Objects.equals(case1[i], case2[i])) {
                diffPosList.add(i);
            }
        }
        return diffPosList;
    }

    /**
     * 验证一系列步骤中船的位置是否合理，并翻译状态
     */
    private static String performCaseChain(List<Integer[]> caseChain) {
        Map<Integer, String> characterMap = new HashMap<>();
        characterMap.put(0, "妈妈");
        characterMap.put(1, "爸爸");
        characterMap.put(2, "哥哥");
        characterMap.put(3, "妹妹");
        characterMap.put(4, "路人");
        Map<Integer, String> bankMap = new HashMap<>();
        bankMap.put(0, "南岸");
        bankMap.put(1, "北岸");

        StringBuilder sb = new StringBuilder();

        int boatPos = 0;
        for (int i = 0; i < caseChain.size() - 1; i++) {
            Integer[] case1 = caseChain.get(i);
            Integer[] case2 = caseChain.get(i + 1);
            List<Integer> diffPosList = getDiffPosList(case1, case2);
            Integer newBoatPos = getBoatPos(case1, case2);

            sb.append(characterMap.get(diffPosList.get(0)));
            if (diffPosList.size() == 2) {
                sb.append("和").append(characterMap.get(diffPosList.get(1)));
            }
            sb.append("从")
                    .append(bankMap.get(boatPos))
                    .append("过河到")
                    .append(bankMap.get(newBoatPos))
                    .append("\n");

            if (newBoatPos == null || boatPos == newBoatPos) {
                return null;
            }


            boatPos = newBoatPos;
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        // 列出所有可能的 case
        List<Integer[]> possibleCases = IntStream.range(0b00000, 0b11111 + 0b1)
                .mapToObj(Integer::toBinaryString)
                // 0 - 4 代表 妈妈 爸爸 哥哥 妹妹 路人
                .map(bStr -> Arrays.stream(String.format("%5s", bStr).replace(' ', '0').split(""))
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new))
                // 魅魔
                .filter(stateArr -> Objects.equals(stateArr[0], stateArr[3])
                        || Stream.of(stateArr[1], stateArr[2], stateArr[4])
                        .filter(state -> Objects.equals(state, stateArr[0]))
                        .count() != 1)
                // 鬼父
                .filter(
                        stateArr -> !(Objects.equals(stateArr[1], stateArr[3])
                                && Arrays.stream(stateArr)
                                .filter(state -> Objects.equals(state, stateArr[1]))
                                .count() == 2)
                )
                // 骨科
                .filter(
                        stateArr -> !(Objects.equals(stateArr[2], stateArr[3])
                                && Arrays.stream(stateArr)
                                .filter(state -> Objects.equals(state, stateArr[2]))
                                .count() == 2)
                )
                // 基佬
                .filter(
                        stateArr -> !(Objects.equals(stateArr[2], stateArr[4])
                                && Arrays.stream(stateArr)
                                .filter(state -> Objects.equals(state, stateArr[2]))
                                .count() == 2)
                )
                .collect(Collectors.toList());


        // 每个 case 当作图中的一个节点
        Graph<Integer[], DefaultEdge> graph = GraphTypeBuilder.<Integer[], DefaultEdge>directed()
                .edgeClass(DefaultEdge.class)
                .weighted(false)
                .allowingSelfLoops(true)
                .allowingMultipleEdges(true)
                .buildGraph();
        possibleCases.forEach(graph::addVertex);

        // 如果两个 case 可以相互转换（过河），那么就在两个节点之间连一条边
        for (int i = 0; i < possibleCases.size(); i++) {
            for (int j = i + 1; j < possibleCases.size(); j++) {
                Integer[] case1 = possibleCases.get(i);
                Integer[] case2 = possibleCases.get(j);
                if (getBoatPos(case1, case2) != null) {
                    graph.addEdge(case1, case2);
                    graph.addEdge(case2, case1);
                }
            }
        }

        AllDirectedPaths<Integer[], DefaultEdge> algorithm = new AllDirectedPaths<>(graph);

        // 最后需要按船靠岸的顺序筛除不合理的情况
        List<String> paths = algorithm.getAllPaths(
                        possibleCases.get(0),
                        possibleCases.get(possibleCases.size() - 1),
                        true,
                        10
                )
                .stream()
                .map(GraphPath::getVertexList)
                .map(path -> performCaseChain(path))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(s -> Math.toIntExact(s.split("\n").length)))
                .limit(5)
                .collect(Collectors.toList());

        paths.forEach(System.out::println);
    }
}
