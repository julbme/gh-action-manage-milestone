package me.julb.applications.github.actions;

/**
 * The input milestone state. <br>
 * @author Julb.
 */
enum InputMilestoneState {
    /**
     * The milestone needs to be open.
     */
    OPEN,

    /**
     * The milestone needs to be closed.
     */
    CLOSED,

    /**
     * The milestone needs to be deleted.
     */
    DELETED;
}