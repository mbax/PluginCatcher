package org.kitteh.catcher;

public class CaughtInTheAct extends Exception {

    private static final long serialVersionUID = 1402679636985159699L;

    public CaughtInTheAct(Throwable t) {
        super(" [ALERT] This is the exception to report back. Save this.", t);
    }
}
