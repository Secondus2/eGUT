package test.junit.oldTests;

import java.util.function.Predicate;

import org.junit.Test;

import spatialRegistry.Area;
import spatialRegistry.Entry;
import spatialRegistry.splitTree.SplitTree;

public class PeriodicTest {

	@Test
	public void periodicTest()
	{
		SplitTree<Double> tree = new SplitTree<Double>(1, 5,
				new double[] {0.0}, new double[] {1.0}, new boolean[] { true });
//		
//		Predicate<Area> test = new Entry(new double[] {0.0}, new double[] {0.4}, null);
//		
//		Entry myEntr = new Entry(new double[] {1.0}, new double[] {0.1}, null);
////		
//		System.out.println(String.valueOf(test.test(myEntr)));
	}
}
