package com.financetoolkit.planner.model;

/** How comfortable a plan is, from best to worst. */
public enum PlanVerdict {
    /** Money left over every month after EMI and investing. */
    AFFORDABLE,
    /** It fits, but with less than 10% of income left as a buffer. */
    TIGHT,
    /** It fits, but the EMI eats more than 40% of income. */
    RISKY,
    /** Income doesn't cover expenses + EMI + investing. */
    NOT_AFFORDABLE
}
