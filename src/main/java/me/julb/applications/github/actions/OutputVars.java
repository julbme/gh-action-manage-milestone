package me.julb.applications.github.actions;

/**
 * The output variables. <br>
 * @author Julb.
 */
enum OutputVars {
    /**
     * The milestone number ID.
     */
    NUMBER("number");

    /**
     * The variable name.
     */
    private String key;

    /**
     * Default constructor.
     * @param key the key name.
     */
    private OutputVars(String key) {
        this.key = key;
    }

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public String key() {
        return key;
    }
}