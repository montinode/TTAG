#!/usr/bin/env python3
"""
calculate_sqrt_sum.py - Computes the telescoping sum of the form:

    S = 1/(√1+√5) + 1/(√2+√6) + 1/(√3+√7) + ... + 1/(√n+√(n+4))

Each term is rationalised by multiplying numerator and denominator by the
conjugate of the denominator:

    1/(√k + √(k+4)) × (√(k+4) − √k)/(√(k+4) − √k)
        = (√(k+4) − √k) / ((k+4) − k)
        = (√(k+4) − √k) / 4

Summing from k=1 to k=n gives a telescoping series:

    S = (1/4) × [(√5−√1) + (√6−√2) + (√7−√3) + (√8−√4)
                + (√9−√5) + (√10−√6) + ... + (√(n+4)−√n)]

After cancellation only the last four minus the first four terms survive:

    S = (1/4) × (√(n+1) + √(n+2) + √(n+3) + √(n+4) − √1 − √2 − √3 − √4)

The default call solves the specific problem stated in the issue:
    n = 2022  →  S = (√2023 + √2024 + √2025 + √2026 − 1 − √2 − √3 − 2) / 4
"""

import math


def sqrt_sum_direct(n: int) -> float:
    """Compute the sum numerically term-by-term (for verification).

    Args:
        n: Number of terms; the k-th term is 1/(√k + √(k+4)) for k=1..n.

    Returns:
        The numerical value of the sum.
    """
    if n < 1:
        raise ValueError("n must be a positive integer")
    return sum(1.0 / (math.sqrt(k) + math.sqrt(k + 4)) for k in range(1, n + 1))


def sqrt_sum_analytical(n: int) -> float:
    """Compute the sum using the closed-form telescoping result.

    After rationalisation and telescoping:
        S = (√(n+1) + √(n+2) + √(n+3) + √(n+4) − √1 − √2 − √3 − √4) / 4

    Args:
        n: Number of terms; the k-th term is 1/(√k + √(k+4)) for k=1..n.

    Returns:
        The exact closed-form value of the sum.
    """
    if n < 1:
        raise ValueError("n must be a positive integer")
    numerator = (
        math.sqrt(n + 1)
        + math.sqrt(n + 2)
        + math.sqrt(n + 3)
        + math.sqrt(n + 4)
        - 1.0
        - math.sqrt(2)
        - math.sqrt(3)
        - 2.0
    )
    return numerator / 4.0


def main():
    n = 2022
    result = sqrt_sum_analytical(n)
    print(
        f"Sum for n=1 to {n} of 1/(√k + √(k+4)):\n"
        f"  Analytical : {result:.10f}\n"
        f"  Direct sum : {sqrt_sum_direct(n):.10f}"
    )


if __name__ == "__main__":
    main()
