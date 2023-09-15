package nl.tue.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRandomGenerator {

	@Test
	public void testExponential() {
		long sum = 0;
		
		for (int i = 0; i < 100; i++) {
			long rn = RandomGenerator.generateExponential(10);
			sum += rn;		
			assertTrue(rn >= 0);
		}
		
		assertTrue("This may be false due to a random effect. Try again.", sum > 800);
		assertTrue("This may be false due to a random effect. Try again.", sum < 1200);
	}

	@Test
	public void testNormal() {
		long sum = 0;
		
		for (int i = 0; i < 100; i++) {
			long rn = RandomGenerator.generateNormal(10,2);
			sum += rn;		
			assertTrue(rn >= 0);
		}
		
		assertTrue("This may be false due to a random effect. Try again.", sum > 900);
		assertTrue("This may be false due to a random effect. Try again.", sum < 1100);
	}

	@Test
	public void testUniform() {
		long sum = 0;
		
		for (int i = 0; i < 100; i++) {
			long rn = RandomGenerator.generateUniform(21);
			sum += rn;		
			assertTrue(rn >= 0);
		}
		
		assertTrue("This may be false due to a random effect. Try again.", sum > 900);
		assertTrue("This may be false due to a random effect. Try again.", sum < 1100);
	}
}
