#!/usr/bin/env python3
"""
Tests for calculate_sqrt_sum.py.

Validates both the direct (brute-force) and analytical (telescoping) approaches
for computing the sum  Σ 1/(√k + √(k+4))  for k = 1 … n.
"""

import math
import sys
import os
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import calculate_sqrt_sum


class TestSqrtSumDirect(unittest.TestCase):
    """Tests for sqrt_sum_direct."""

    def test_single_term(self):
        """n=1 should equal 1/(√1 + √5)."""
        expected = 1.0 / (math.sqrt(1) + math.sqrt(5))
        self.assertAlmostEqual(calculate_sqrt_sum.sqrt_sum_direct(1), expected, places=10)

    def test_two_terms(self):
        """n=2 should equal 1/(√1+√5) + 1/(√2+√6)."""
        expected = (
            1.0 / (math.sqrt(1) + math.sqrt(5))
            + 1.0 / (math.sqrt(2) + math.sqrt(6))
        )
        self.assertAlmostEqual(calculate_sqrt_sum.sqrt_sum_direct(2), expected, places=10)

    def test_four_terms(self):
        """n=4 checks the first complete cycle of the telescoping pattern."""
        expected = sum(
            1.0 / (math.sqrt(k) + math.sqrt(k + 4)) for k in range(1, 5)
        )
        self.assertAlmostEqual(calculate_sqrt_sum.sqrt_sum_direct(4), expected, places=10)

    def test_invalid_n_raises(self):
        """n <= 0 must raise ValueError."""
        with self.assertRaises(ValueError):
            calculate_sqrt_sum.sqrt_sum_direct(0)
        with self.assertRaises(ValueError):
            calculate_sqrt_sum.sqrt_sum_direct(-5)


class TestSqrtSumAnalytical(unittest.TestCase):
    """Tests for sqrt_sum_analytical (closed-form telescoping result)."""

    def test_single_term(self):
        """Analytical formula should match direct calculation for n=1."""
        self.assertAlmostEqual(
            calculate_sqrt_sum.sqrt_sum_analytical(1),
            calculate_sqrt_sum.sqrt_sum_direct(1),
            places=10,
        )

    def test_four_terms(self):
        """Analytical formula should match direct calculation for n=4."""
        self.assertAlmostEqual(
            calculate_sqrt_sum.sqrt_sum_analytical(4),
            calculate_sqrt_sum.sqrt_sum_direct(4),
            places=10,
        )

    def test_hundred_terms(self):
        """Analytical formula should match direct calculation for n=100."""
        self.assertAlmostEqual(
            calculate_sqrt_sum.sqrt_sum_analytical(100),
            calculate_sqrt_sum.sqrt_sum_direct(100),
            places=8,
        )

    def test_known_closed_form_n4(self):
        """For n=4 the closed form is (√5+√6+√7+√8−1−√2−√3−2) / 4."""
        expected = (
            math.sqrt(5) + math.sqrt(6) + math.sqrt(7) + math.sqrt(8)
            - 1.0 - math.sqrt(2) - math.sqrt(3) - 2.0
        ) / 4.0
        self.assertAlmostEqual(
            calculate_sqrt_sum.sqrt_sum_analytical(4), expected, places=12
        )

    def test_invalid_n_raises(self):
        """n <= 0 must raise ValueError."""
        with self.assertRaises(ValueError):
            calculate_sqrt_sum.sqrt_sum_analytical(0)
        with self.assertRaises(ValueError):
            calculate_sqrt_sum.sqrt_sum_analytical(-1)


class TestSqrtSumN2022(unittest.TestCase):
    """Validates the specific problem: n = 2022."""

    def test_analytical_matches_direct(self):
        """Analytical and direct results must agree to 8 decimal places."""
        analytical = calculate_sqrt_sum.sqrt_sum_analytical(2022)
        direct = calculate_sqrt_sum.sqrt_sum_direct(2022)
        self.assertAlmostEqual(analytical, direct, places=8)

    def test_closed_form_value(self):
        """Check the exact closed-form expression for n=2022."""
        expected = (
            math.sqrt(2023) + math.sqrt(2024) + math.sqrt(2025) + math.sqrt(2026)
            - math.sqrt(1) - math.sqrt(2) - math.sqrt(3) - math.sqrt(4)
        ) / 4.0
        self.assertAlmostEqual(
            calculate_sqrt_sum.sqrt_sum_analytical(2022), expected, places=12
        )

    def test_result_is_positive(self):
        """The sum must be a positive finite number."""
        result = calculate_sqrt_sum.sqrt_sum_analytical(2022)
        self.assertGreater(result, 0)
        self.assertTrue(math.isfinite(result))


if __name__ == "__main__":
    unittest.main()
