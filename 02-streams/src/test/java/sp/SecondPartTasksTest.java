package sp;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static sp.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {

        List<String> paths = Arrays.asList(
                "src/test/java/sp/tests/Battlestar.txt",
                "src/test/java/sp/tests/Stargate.txt",
                "src/test/java/sp/tests/StarTrek.txt"
        );

        List<String> answer1 = Arrays.asList(
                "All Battlestar Galactica productions share the premise that in a distant part of the universe, a human civilization has extended to a group of planets known as the Twelve Colonies, to which they have migrated from their ancestral homeworld of Kobol.",
                "Of the entire Colonial battle fleet, only the Battlestar Galactica, a gigantic battleship and spacecraft carrier, appears to have survived the Cylon attack."
        );

        List<String> answer2 = Arrays.asList(
                "In 2011, Stargate Universe, the last Stargate program on television, ended its run and the franchise has been in limbo since.",
                "However, a variety of other media either ignore this main story arc, or reset it, while maintaining essential elements that define the franchise (mainly, the inclusion of a Stargate device).",
                "These include the 2002 animated series Stargate Infinity, and a possible new film series once again directed by Roland Emmerich, announced as being under consideration in 2014."
        );

        List<String> answer3 = Arrays.asList(
                "In creating the first Star Trek, Roddenberry was inspired by Westerns, Wagon Train, the Horatio Hornblower novels and Gulliver's Travels.",
                "These adventures continued in the short-lived Star Trek: The Animated Series and six feature films."
        );

        List<String> result1 = findQuotes(paths, "Battlestar");
        List<String> result2 = findQuotes(paths, "Stargate");
        List<String> result3 = findQuotes(paths, "Star Trek");

        Collections.sort(answer1);
        Collections.sort(answer2);
        Collections.sort(answer3);

        Collections.sort(result1);
        Collections.sort(result2);
        Collections.sort(result3);

        assertEquals(answer1, result1);
        assertEquals(answer2, result2);
        assertEquals(answer3, result3);
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI * 0.25, piDividedBy4(), 0.01);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();

        compositions.put("Chewbacca", Arrays.asList(
                "Aaaaaaaaaaaaaaaarrrgh!",
                "Grrf."
        ));

        compositions.put("Han Solo", Arrays.asList(
                "Boring conversation anyway. LUKE, WE'RE GONNA HAVE COMPANY!",
                "Hokey religions and ancient weapons are no match for a good blaster at your side, kid."
        ));

        compositions.put("Princess Leia Organa", Arrays.asList(
                "Aren't you a little short for a stormtrooper?",
                "I don't know who you are or where you came from, but from now on you'll do as I tell you, okay?",
                "Help me, Obi-Wan Kenobi; you're my only hope."
        ));

        assertEquals("Princess Leia Organa", findPrinter(compositions));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> luke = new HashMap<>();
        luke.put("green lightsaber", 3);
        luke.put("blue lightsaber", 2);

        Map<String, Integer> anakin = new HashMap<>();
        anakin.put("blue lightsaber", 1);
        anakin.put("red lightsaber", 4);
        anakin.put("stormtrooper", 1000);

        Map<String, Integer> kylo = new HashMap<>();
        kylo.put("red lightsaber", 2);
        kylo.put("stormtrooper", 1500);

        Map<String, Integer> globalOrder = new HashMap<>();
        globalOrder.put("green lightsaber", 3);
        globalOrder.put("blue lightsaber", 3);
        globalOrder.put("red lightsaber", 6);
        globalOrder.put("stormtrooper", 2500);

        assertEquals(globalOrder, calculateGlobalOrder(Arrays.asList(luke, anakin, kylo)));
    }
}