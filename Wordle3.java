import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.Set;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/*
Matt Parker
Can you find: five five-letter words with twenty-five unique letters?

FJORD
GUCKS
NYMPH
VIBEX
WALTZ
Q

Matt:     32 days
Benjamin: 15 minutes
Fred:      1 second

Constraints:
- No duplicate letters (valid words have 5 unique characters)
- Order of letters irrelevant (ignore anagrams during search)
- i.e. Word is Set of Characters
Representation:
- 32-bit number
- 26 bits for the letters A-Z
- 6 bits unused

25                       0
ABCDEFGHIJKLMNOPQRSTUVWXYZ
   1 1   1    1  1         fjord

---D-F---J----O--R-------- fjord
--C---G---K-------S-U----- gucks
-------------------------- fjord AND gucks (INTERSECTION)
--CD-FG--JK---O--RS-U----- fjord OR gucks (UNION)
*/

public class Wordle {
    private static List<String> rawWords;
    private static Map<Integer, List<String>> groupByEncode;

    public static void main(String[] args) throws IOException {
        Stopwatch stopwatch = new Stopwatch();

        rawWords = new ArrayList<>();
	//        rawWords.addAll(Files.readAllLines(Path.of("wordle-answers-alphabetical.txt")));
        rawWords.addAll(Files.readAllLines(Path.of("../wordle-nyt-allowed-guesses.txt")));
        System.out.println(rawWords.size() + " raw words");

        int[] cookedWords = rawWords.stream()
                // A---E------L---P---------- apple
                // --C-----I--L-----------XY- cylix
                // --C-----I--L-----------XY- xylic
                .mapToInt(Wordle::encodeWord)
                // remove words with duplicate characters
                .filter(word -> Integer.bitCount(word) == 5)
                .sorted()
                // remove anagrams
                .distinct()
                .toArray();
        System.out.println(cookedWords.length + " cooked words\n");



	/*Map<BigDecimal, List<Item>> groupByPriceMap = 
			items.stream().collect(Collectors.groupingBy(Item::getPrice));
	*/
 groupByEncode =   rawWords.stream().collect(Collectors.groupingBy(Wordle::encodeWord));
  int[] cookedList = groupByEncode.keySet().stream()
    .mapToInt(Integer::intValue)
      .filter(word -> Integer.bitCount(word) == 5)
                .sorted()
                // remove anagrams
                .distinct()
   .toArray(); 

  groupByEncode.forEach((key, value) -> System.out.println(String.format("%32s", Integer.toBinaryString(key)).replace(' ', '0')  + "==" + key + ":" + value));

	System.out.println(Arrays.binarySearch(cookedWords, 1 << 24));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 19));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 14));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 9));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 4));
        System.out.println(Arrays.binarySearch(cookedWords, 0));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 27));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 26));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 25));
        System.out.println(Arrays.binarySearch(cookedWords, 1 << 23));
	/*
b =	(-(insertion point) - 1)
b =	-i - 1
b+1 =	-i
-b-1 =	i
	*/
	int maxA = cookedWords.length + 1 + Arrays.binarySearch(cookedWords, 1 << 24);
	int maxB = cookedWords.length + 1 + Arrays.binarySearch(cookedWords, 1 << 19);
	int maxC = cookedWords.length + 1 + Arrays.binarySearch(cookedWords, 1 << 14);
	int maxD = cookedWords.length + 1 + Arrays.binarySearch(cookedWords, 1 << 9);
	int maxE = cookedWords.length + 1 + Arrays.binarySearch(cookedWords, 1 << 4) ;
	for(int i = 0; i < cookedWords.length / 2; i++)
	    {
		int temp = cookedWords[i];
		cookedWords[i] = cookedWords[cookedWords.length - i - 1];
		cookedWords[cookedWords.length - i - 1] = temp;
	    }
        System.out.println(maxA);
        System.out.println(maxB);
        System.out.println(maxC);
        System.out.println(maxD);
        System.out.println(maxE);
        System.out.println(Integer.toBinaryString(cookedWords[197]));
        System.out.println(Integer.toBinaryString(cookedWords[196]));
        System.out.println(Integer.toBinaryString(cookedWords[198]));
        System.out.println("ab".charAt(0));
        System.out.println(1 << "ab".charAt(0));
        System.out.println(1 << 'a');
        System.out.println(1 << 'b');
        System.out.println(1 << 'c');
        System.out.println('a');
	/*		for(int i = 0; i < cookedWords.length; i++)
	    System.out.println(Integer.toBinaryString(cookedWords[i]));
	*/


	

        // 54 MB  skip[i][j] is the first i-intersection-free index >= j
        short[][] skip = new short[cookedWords.length][cookedWords.length + 1];
        for (int i = 0; i < cookedWords.length; ++i) {
            skip[i][cookedWords.length] = (short) cookedWords.length; // 5176
            int A = cookedWords[i];
            for (int j = cookedWords.length - 1; j >= i; --j) {
                int B = cookedWords[j];
                skip[i][j] = ((A & B) == 0) ? (short) j : skip[i][j + 1];
            }
        }
        // 20 KB  first[i] is identical to skip[i][i] but hot in cache
        int[] first = new int[cookedWords.length];
        for (int i = 0; i < cookedWords.length; ++i) {
            first[i] = skip[i][i];
        }

        // for (int i = 0; i < cookedWords.length; ++i) {
        IntStream.range(0, maxA).parallel().forEach((int i) -> {

            // for (int j = i + firstStep[i]; j < cookedWords.length; ++j) {
		//            for (int j = first[i]; j < maxB; j++, j = skip[i][j]) {
		for (int j = first[i]; j < maxB; j++, j = skip[i][j]) {

                // for (int k = j + firstStep[j]; k < cookedWords.length; ++k) {
                for (int k = first[j]; k < maxC; k++) {
		    /*                    k = skip[i][k]; // this likely reduces the do-while loop by 1 iteration
                    k = skip[j][k];
                    */
		    int oldK;
		    
                    do{
			oldK = skip[i][k];
                        k = skip[j][oldK];
                    } while(k != oldK); // skip until i and j don't collide with k
                    if (k == cookedWords.length) break; // skipped to end of k, next j

                    // for (int l = k + firstStep[k]; l < cookedWords.length; ++l) {
                    for (int l = first[k]; l < maxD; l++) {
			int oldL;
                        do{
                            oldL = skip[i][l];
                            l = skip[j][oldL];
                            l = skip[k][l];
                        } while(l != oldL);
                        if (l == cookedWords.length) break;


                        // for (int m = l + firstStep[l]; m < cookedWords.length; ++m) {
                        for (int m = first[l]; m < maxE; m++) {
			    int oldM;
                            do{
				/*
				  It's interesting to think about what kind of performance gain
				  this whole skip matrix gets you over just interatively filter
				  for a new list at every depth.
				  This is looking for fixed points of stair case functions.

				 */
                                oldM = skip[i][m];
                                m = skip[j][oldM];
                                m = skip[k][m];
                                m = skip[l][m];
                            } while(m != oldM);
                            if (m == cookedWords.length) break;

                            // since the words are not needed for comparisson, only fetch them for printing
                            int A = cookedWords[i];
                            int B = cookedWords[j];               
                            int C = cookedWords[k];
                            int D = cookedWords[l];
                            int E = cookedWords[m];

                            System.out.printf("%s\n%s\n\n",
                                    stopwatch.elapsedTime(),
                                    decodeWords(A, B, C, D, E));
                        }
                    }
                }
            }
        });
        System.out.println(stopwatch.elapsedTime());
    }

    private static String decodeWord(int word) {
        // --C-----I--L-----------XY- cylix/xylic
        return
	    /*
Blah! Needless O(n)
	      rawWords.stream()
                .filter(raw -> encodeWord(raw) == word)
	    */

	groupByEncode.get(word).stream()
                .collect(Collectors.joining("/", visualizeWord(word) + " ", ""));
    }

    private static String decodeWords(int... words) {
        // ----E-----K-M--P---T------ kempt
        // ---D---H------O------V---Z vozhd
        // --C-----I--L-----------XY- cylix/xylic
        // -B----G------N---R--U----- brung
        // A----F----------Q-S---W--- waqfs
        return Arrays.stream(words)
                .mapToObj(Wordle::decodeWord)
                .collect(Collectors.joining("\n"));
    }

    private static int encodeWord(String raw) {
	String letterFreq = "ETAOINSRHDLUCMFYWGPBVKXQJZ".toLowerCase();
	//                   01234567890123456789012345
	    // Want to make an array from int-> int
	    
	    /*
	    Give it raw.charAt(i) - 'a' which is 'a' -> 0, 'b' -> 1...
	    and it will return 0 for 'e'(5), 1 for 't'(20 or whatever).
	    want an array that looks like [x,x,x,x,x,0,x,x,x,....1 (in index 20), x,x,x]
can use
	     */
	//        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	int[] mapFromCharToIndex = new int[]{2, 19, 12, 9, 0, 14, 17, 8, 4,24, 21,
		10, 13, 5, 3, 18, 23, 7 , 6, 1, 11, 20, 16, 22, 15, 25};

	    /* alphabet.stream().mapToInt( letter -> letterFreq.find( letter ) ) ;*/

	    
	//    1 1   1    1  1         fjord
        int bitset = 0;
        for (int i = 0; i < raw.length(); ++i) {
	    //	                bitset |= 1 << raw.charAt(i);
	    /* so charAt returns a char which gets converted to an ASCII
'a' => 97
'b' => 98
...
But the bit shift operator << is implementation-defined when n is > 32, but the common implementation
is shift by n % 32. And 97%32 happens to be only 1, so a gets shifted by 1, even though we want to it by shifted by 0.

	     */
	               bitset |= 1 << mapFromCharToIndex[raw.charAt(i) - 'a'];
        }
	//	            System.out.println(bitset + '\n');
        return bitset;
    }

    private static String visualizeWord(int word) {
        //    1 1   1    1  1        
        // ---D-F---J----O--R--------
        char[] a = new char[26];
        word <<= 6;
        for (int i = 0; i < a.length; ++i, word <<= 1) {
	    //            a[i] = (word < 0) ? (char) ('A' + i) : '-';
	    a[i] = (word < 0) ? "ETAOINSRHDLUCMFYWGPBVKXQJZ".charAt(25-i) : '-';
        }
        return new String(a);
    }
}

class Stopwatch {
    private final Instant start = Instant.now();

    public String elapsedTime() {
        Instant now = Instant.now();
        Duration duration = Duration.between(start, now).truncatedTo(ChronoUnit.MILLIS);
        String formatted = DateTimeFormatter.ISO_LOCAL_TIME.format(duration.addTo(LocalTime.of(0, 0)));
        return "[" + formatted + "] ";
    }
}
// Without modifications runs at [00:00:01.6ish]
// with maxA,B,C,D,E gets to [00:00:00.274]
/*
E 	21912 	  	E 	12.02
T 	16587 	  	T 	9.10
A 	14810 	  	A 	8.12
O 	14003 	  	O 	7.68
I 	13318 	  	I 	7.31
N 	12666 	  	N 	6.95
S 	11450 	  	S 	6.28
R 	10977 	  	R 	6.02
H 	10795 	  	H 	5.92
D 	7874 	  	D 	4.32
L 	7253 	  	L 	3.98
U 	5246 	  	U 	2.88
C 	4943 	  	C 	2.71
M 	4761 	  	M 	2.61
F 	4200 	  	F 	2.30
Y 	3853 	  	Y 	2.11
W 	3819 	  	W 	2.09
G 	3693 	  	G 	2.03
P 	3316 	  	P 	1.82
B 	2715 	  	B 	1.49
V 	2019 	  	V 	1.11
K 	1257 	  	K 	0.69
X 	315 	  	X 	0.17
Q 	205 	  	Q 	0.11
J 	188 	  	J 	0.10
Z 	128 	  	Z 	0.07

*/
