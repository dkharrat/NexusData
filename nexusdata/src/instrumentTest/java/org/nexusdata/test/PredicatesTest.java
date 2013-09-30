package com.pixineers.nexusdata.test;

import junit.framework.TestCase;

import com.pixineers.nexusdata.predicate.ExpressionBuilder;
import com.pixineers.nexusdata.predicate.Predicate;

public class PredicatesTest extends TestCase {

    class Book {
        String title;
        int pages;

        Book(String title, int pages) {
            this.title = title;
            this.pages = pages;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimplePredicateTrue() throws Throwable {
        Predicate p = ExpressionBuilder.constant("1").eq("1").getPredicate();
        assertTrue(p.evaluate(null));
    }

    public void testPredicateWithFieldPathExpression() throws Throwable {

        Book book = new Book("Book one", 362);

        Predicate p = ExpressionBuilder.field("title").eq("Book one").getPredicate();
        assertTrue(p.evaluate(book));

        p = ExpressionBuilder.field("pages").eq(362).getPredicate();
        assertTrue(p.evaluate(book));

        p = ExpressionBuilder.field("pages").eq(363).getPredicate();
        assertFalse(p.evaluate(book));
    }

    public void testSimplePredicateFalse() throws Throwable {
        Predicate p = ExpressionBuilder.constant("1").eq("2").getPredicate();
        assertFalse(p.evaluate(null));
    }

    public void testComparisonPredicateTrue() throws Throwable {
        Predicate p = ExpressionBuilder.constant(10).gt(5).getPredicate();
        assertTrue(p.evaluate(null));
    }

    public void testComparisonPredicateFalse() throws Throwable {
        Predicate p = ExpressionBuilder.constant(10).gt(15).getPredicate();
        assertFalse(p.evaluate(null));
    }

    public void testCompoundPredicateTrue() throws Throwable {
        Predicate p = ExpressionBuilder.constant(10).gt(5).and(ExpressionBuilder.constant(8).lt(12)).getPredicate();
        assertTrue(p.evaluate(null));
    }

    public void testCompoundPredicateFalse() throws Throwable {
        Predicate p = ExpressionBuilder.constant(10).gt(5).and(ExpressionBuilder.constant(8).lt(6)).getPredicate();
        assertFalse(p.evaluate(null));
    }

    public void testComplexCompoundPredicateTrue() throws Throwable {
        // 10 > 5 || (8 == 8 && 8 < 6)
        Predicate p = ExpressionBuilder.constant(10).gt(5)
                .or(ExpressionBuilder.constant(8).eq(8)
                        .and(ExpressionBuilder.constant(8).lt(6))).getPredicate();
        assertTrue(p.evaluate(null));
    }

    public void testComplexCompoundPredicateFalse() throws Throwable {
        // (10 > 5 || 8 == 8) && 8 < 6
        Predicate p = (ExpressionBuilder.constant(10).gt(5).or(ExpressionBuilder.constant(8).eq(8)).and(ExpressionBuilder.constant(8).lt(6))).getPredicate();
        assertFalse(p.evaluate(null));
    }
}
