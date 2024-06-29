package be.twofold.playground.legsolver;

import java.util.*;

public class LegSolver {
    private static final List<List<String>> Words = List.of(
        List.of(),
        List.of(),
        List.of(),
        List.of(
            "aar",
            "oog",
            "pub",
            "som"
        ),
        List.of(
            "date",
            "doen",
            "elan",
            "erbĳ",
            "exit",
            "onyx",
            "rage",
            "slee",
            "tong"
        ),
        List.of(
            "agave",
            "animo",
            "bijou",
            "foton",
            "gelĳk",
            "gevat",
            "kiosk",
            "kneep",
            "opper",
            "retro",
            "ritme",
            "schub",
            "sober",
            "speld",
            "tango",
            "truck"
        ),
        List.of(
            "fresia",
            "hierna",
            "krokus",
            "lacune",
            "lintje",
            "rimboe"
        ),
        List.of(
            "chassis",
            "rapport",
            "senator",
            "sirocco",
            "smakken"
        ),
        List.of(
            "afgetobd",
            "berghond",
            "botsauto",
            "geronnen",
            "pistolet",
            "tiramisu"
        )
    );

    private static final List<String> BoardStrings = List.of(
        "··· ·· ·",
        "       ·",
        " ·· ·· ·",
        "        ",
        " ··· ·· ",
        "     ·· ",
        "····    ",
        "     · ·",
        " · ·   ·",
        " · ···   ··     ",
        "       ·    · · ",
        " · · · · ·· ·   ",
        "     · · ·    · ",
        "·· ·       ·· · ",
        "     ·· ·· ·    ",
        "· · · ·     · ··",
        "· ·     · ·     ",
        "· · · ·     ····",
        "        · ·     ",
        "· · · ·     ··· ",
        "· ·     · ·     "
    );

    public static void main(String[] args) {
        Board board = new Board(16, 21);
        board.fillFrom(BoardStrings);
        board.block(8, 0, 8, 9);

        int totalWords = Words.stream()
            .mapToInt(List::size)
            .sum();

        List<List<String>> mutableWords = new ArrayList<>(Words);
        mutableWords.replaceAll(ArrayList::new);

        Set<Position> positions = new HashSet<>(board.analyzePossiblePositions());
        assert positions.size() == totalWords : "Not all words are found";

        Position pos = new Position(11, 20, 5, Direction.Horizontal);
        board.fill("schub", pos);
        positions.remove(pos);

        while (!positions.isEmpty()) {
            for (Position position : new ArrayList<>(positions)) {
                if (!board.testIfPositionIncomplete(position)) {
                    continue;
                }
                List<String> wordsOfLength = mutableWords.get(position.length());
                List<String> wordsThatFit = new ArrayList<>();
                for (String word : wordsOfLength) {
                    if (board.testIfWordFits(word, position)) {
                        wordsThatFit.add(word);
                    }
                }

                if (wordsThatFit.isEmpty()) {
                    continue;
                }
                if (wordsThatFit.size() > 1) {
                    continue;
                }

                String word = wordsThatFit.get(0);
                if (!board.testIfWordFits(word, position)) {
                    continue;
                }
                board.fill(word, position);
                positions.remove(position);
                wordsOfLength.remove(word);
                break;

            }
        }

        System.out.println(board);
    }
}
