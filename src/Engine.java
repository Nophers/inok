import java.util.ArrayList;
import java.util.List;

/*
 * This class is responsible for matching a string against a pattern.
 * @author vKxni 
 * @version 0.0.2.1
 */
public class Engine {
    private final String pattern;

    public Engine(String pattern) {
        this.pattern = pattern;
    }

    public boolean match(String input) {
        List<String> tokens = tokenize(pattern);
        NFA nfa = buildNFA(tokens);
        return nfa.match(input);
    }

    /**
     * Tokenizes a regular expression pattern into a list of tokens.
     *
     * @param pattern the regular expression pattern to tokenize
     * @return a list of tokens
     */
    private List<String> tokenize(String pattern) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            switch (c) {
                case '\\':
                    i++;
                    if (i < pattern.length()) {
                        sb.append('\\').append(pattern.charAt(i));
                    }
                    break;
                case '[':
                case '{':
                    int j = findClosingBracket(pattern, i, c == '[' ? ']' : '}');
                    if (j < pattern.length()) {
                        sb.append(pattern, i, j + 1);
                    }
                    i = j + 1;
                    break;
                default:
                    sb.append(c);
                    break;
            }
            i++;
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }
        return tokens;
    }

    private int findClosingBracket(String pattern, int startIndex, char bracket) {
        int j = startIndex + 1;
        while (j < pattern.length() && pattern.charAt(j) != bracket) {
            j++;
        }
        return j;
    }

    /**
     * Builds an NFA from a list of tokens representing a regular expression
     * pattern.
     *
     * @param tokens the list of tokens representing the regular expression pattern
     * @return an NFA representing the regular expression pattern
     */
    private NFA buildNFA(List<String> tokens) {
        NFA nfa = new NFA();
        State startState = nfa.getStartState();
        State endState = nfa.getEndState();
        List<State> currentStates = new ArrayList<>();
        currentStates.add(startState);

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            switch (token) {
                case "|" -> handleOrToken(nfa, currentStates, endState);
                case "*" -> handleStarToken(nfa, currentStates, startState);
                case "+" -> handlePlusToken(nfa, currentStates, startState);
                case "?" -> handleQuestionToken(nfa, currentStates, endState, startState);
                case "^" -> handleCaretToken(nfa, currentStates, endState, startState, i);
                case "$" -> handleDollarToken(nfa, currentStates, endState, i, tokens.size());
                case "." -> handleDotToken(nfa, currentStates);
                case "(" -> i = handleOpenParenToken(nfa, tokens, currentStates, i);
                case "[" -> i = handleOpenBracketToken(nfa, tokens, currentStates, i);
                case "{" -> i = handleOpenBraceToken(nfa, tokens, currentStates, i);
                default -> handleDefaultToken(nfa, currentStates, token);
            }
        }

        for (State currentState : currentStates) {
            currentState.addTransition(endState, null);
        }

        return nfa;
    }

    /**
     * Handles the "|" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param endState      the end state of the NFA
     * @throws IllegalArgumentException if the current states list has more than one
     *                                  state
     */
    private void handleOrToken(NFA nfa, List<State> currentStates, State endState) {
        if (currentStates.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        State currentState = currentStates.get(0);
        State newState1 = nfa.addState();
        State newState2 = nfa.addState();
        currentState.addTransition(newState1, null);
        currentState.addTransition(newState2, null);
        State endState1 = nfa.addState();
        State endState2 = nfa.addState();
        newState1.addTransition(endState1, null);
        newState2.addTransition(endState2, null);
        endState1.addTransition(endState, null);
        endState2.addTransition(endState, null);
        currentStates.clear();
        currentStates.add(endState1);
        currentStates.add(endState2);
    }

    /**
     * Handles the "*" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param startState    the start state of the NFA
     * @throws IllegalArgumentException if the current states list has more than one
     *                                  state
     */
    private void handleStarToken(NFA nfa, List<State> currentStates, State startState) {
        if (currentStates.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        State currentState = currentStates.get(0);
        State newState1 = nfa.addState();
        State newState2 = nfa.addState();
        currentState.addTransition(newState1, null);
        currentState.addTransition(newState2, null);
        newState1.addTransition(newState2, null);
        newState1.addTransition(nfa.getEndState(), null);
        startState.addTransition(newState2, null);
        currentStates.clear();
        currentStates.add(newState2);
    }

    /**
     * Handles the "+" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param startState    the start state of the NFA
     * @throws IllegalArgumentException if the current states list has more than one
     *                                  state
     */
    private void handlePlusToken(NFA nfa, List<State> currentStates, State startState) {
        if (currentStates.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        State currentState = currentStates.get(0);
        State newState1 = nfa.addState();
        State newState2 = nfa.addState();
        currentState.addTransition(newState1, null);
        currentState.addTransition(newState2, null);
        newState1.addTransition(newState2, null);
        newState1.addTransition(nfa.getEndState(), null);
        startState.addTransition(newState1, null);
        currentStates.clear();
        currentStates.add(newState2);
    }

    /**
     * Handles the "?" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param endState      the end state of the NFA
     * @param startState    the start state of the NFA
     * @throws IllegalArgumentException if the current states list has more than one
     *                                  state
     */
    private void handleQuestionToken(NFA nfa, List<State> currentStates, State endState, State startState) {
        if (currentStates.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        State currentState = currentStates.get(0);
        State newState = nfa.addState();
        currentState.addTransition(newState, null);
        newState.addTransition(endState, null);
        startState.addTransition(newState, null);
        currentStates.clear();
        currentStates.add(newState);
    }

    /**
     * Handles the "^" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param endState      the end state of the NFA
     * @param startState    the start state of the NFA
     * @param i             the index of the "^" token in the list of tokens
     */
    private void handleCaretToken(NFA nfa, List<State> currentStates, State endState, State startState, int i) {
        if (i == 0) {
            State newState = nfa.addState();
            newState.addTransition(endState, null);
            startState.addTransition(newState, null);
            currentStates.clear();
            currentStates.add(newState);
        } else {
            handleDefaultToken(nfa, currentStates, "^");
        }
    }

    /**
     * Handles the "$" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     * @param endState      the end state of the NFA
     * @param i             the index of the "$" token in the list of tokens
     * @param size          the size of the list of tokens
     * @throws IllegalArgumentException if the "$" token is not the last token in
     *                                  the list of tokens
     */
    private void handleDollarToken(NFA nfa, List<State> currentStates, State endState, int i, int size) {
        if (i != size - 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        for (State currentState : currentStates) {
            currentState.addTransition(endState, null);
        }

        currentStates.clear();
        currentStates.add(endState);
    }

    /**
     * Handles the "." token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param currentStates the current states in the NFA
     */
    private void handleDotToken(NFA nfa, List<State> currentStates) {
        State newState = nfa.addState();
        for (State currentState : currentStates) {
            currentState.addTransition(newState, null);
        }
        currentStates.clear();
        currentStates.add(newState);
    }

    /**
     * Handles the "(" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param tokens        the list of tokens representing the regular expression
     *                      pattern
     * @param currentStates the current states in the NFA
     * @param i             the index of the "(" token in the list of tokens
     * @return the index of the corresponding ")" token in the list of tokens
     * @throws IllegalArgumentException if there is no corresponding ")" token in
     *                                  the list of tokens
     */
    private int handleOpenParenToken(NFA nfa, List<String> tokens, List<State> currentStates, int i) {
        int j = i + 1;
        int count = 1;
        List<String> subTokens = new ArrayList<>();

        while (j < tokens.size()) {
            String token = tokens.get(j);
            if (token.equals("(")) {
                count++;
            } else if (token.equals(")")) {
                count--;
                if (count == 0) {
                    break;
                }
            }
            subTokens.add(token);
            j++;
        }

        if (count != 0) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        NFA subNFA = buildNFA(subTokens);
        State subStartState = subNFA.getStartState();
        State subEndState = subNFA.getEndState();

        for (State currentState : currentStates) {
            currentState.addTransition(subStartState, null);
        }

        currentStates.clear();
        currentStates.add(subEndState);

        return j;
    }

    /**
     * Handles the "[" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param tokens        the list of tokens representing the regular expression
     *                      pattern
     * @param currentStates the current states in the NFA
     * @param i             the index of the "[" token in the list of tokens
     * @return the index of the corresponding "]" token in the list of tokens
     * @throws IllegalArgumentException if there is no corresponding "]" token in
     *                                  the list of tokens, or if the contents of
     *                                  the brackets are invalid
     */
    private int handleOpenBracketToken(NFA nfa, List<String> tokens, List<State> currentStates, int i) {
        int j = i + 1;
        boolean negate = false;
        List<Character> chars = new ArrayList<>();

        if (j < tokens.size() && tokens.get(j).equals("^")) {
            negate = true;
            j++;
        }

        while (j < tokens.size() && !tokens.get(j).equals("]")) {
            String token = tokens.get(j);
            if (token.length() == 1) {
                chars.add(token.charAt(0));
            } else if (token.length() == 3 && token.charAt(1) == '-') {
                char startChar = token.charAt(0);
                char endChar = token.charAt(2);
                if (startChar > endChar) {
                    throw new IllegalArgumentException("Invalid expression: " + pattern);
                }
                for (char c = startChar; c <= endChar; c++) {
                    chars.add(c);
                }
            } else {
                throw new IllegalArgumentException("Invalid expression: " + pattern);
            }
            j++;
        }

        if (j == tokens.size()) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        if (chars.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        State newState = nfa.addState();
        for (State currentState : currentStates) {
            for (char c = 0; c < 128; c++) {
                boolean match = chars.contains(c);
                if (negate) {
                    match = !match;
                }
                if (match) {
                    currentState.addTransition(newState, String.valueOf(c));
                }
            }
        }

        currentStates.clear();
        currentStates.add(newState);

        return j;
    }

    /**
     * Handles the "{" token in a regular expression pattern.
     *
     * @param nfa           the NFA being constructed
     * @param tokens        the list of tokens representing the regular expression
     *                      pattern
     * @param currentStates the current states in the NFA
     * @param i             the index of the "{" token in the list of tokens
     * @return the index of the corresponding "}" token in the list of tokens
     * @throws IllegalArgumentException if there is no corresponding "}" token in
     *                                  the list of tokens, or if the contents of
     *                                  the braces are invalid
     */
    private int handleOpenBraceToken(NFA nfa, List<String> tokens, List<State> currentStates, int i) {
        int j = i + 1;
        StringBuilder sb = new StringBuilder();

        while (j < tokens.size() && !tokens.get(j).equals("}")) {
            sb.append(tokens.get(j));
            j++;
        }

        // If we reached the end of the tokens list without finding a closing curly
        // brace, the expression is invalid
        if (j == tokens.size()) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        // Split the substring between the opening and closing curly braces into parts
        // separated by commas
        // If there are not one or two parts, the expression is invalid
        String[] parts = sb.toString().split(",");
        if (parts.length != 1 && parts.length != 2) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        // Parse the minimum and maximum values from the parts
        // If the minimum is negative or greater than the maximum, the expression is
        // invalid
        int min = Integer.parseInt(parts[0]);
        int max = parts.length == 2 ? Integer.parseInt(parts[1]) : min;
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }

        // Repeat the sub-pattern the minimum number of times
        List<String> subTokens = new ArrayList<>();
        for (int k = 0; k < min; k++) {
            // Create a sublist of the tokens that represent the sub-pattern &
            // get the start and end states of the sub-NFA
            subTokens.addAll(tokens.subList(j + 1, tokens.size() - 1));
            NFA subNFA = buildNFA(subTokens);

            // Get the start and end states of the sub-NFA
            State subStartState = subNFA.getStartState();
            State subEndState = subNFA.getEndState();

            // Add transitions from the current states to the start state of the sub-NFA
            for (State currentState : currentStates) {
                currentState.addTransition(subStartState, null);
            }
            // Set the current states to the end state of the sub-NFA
            currentStates.clear();
            currentStates.add(subEndState);
        }

        if (min == max) {
            return j;
        }

        State newState = nfa.addState();
        for (int k = min; k < max; k++) {
            subTokens.addAll(tokens.subList(j + 1, tokens.size() - 1));
            NFA subNFA = buildNFA(subTokens);
            State subStartState = subNFA.getStartState();
            State subEndState = subNFA.getEndState();
            for (State currentState : currentStates) {
                currentState.addTransition(subStartState, null);
            }
            subEndState.addTransition(newState, null);
            currentStates.clear();
            currentStates.add(subEndState);
        }

        currentStates.clear();
        currentStates.add(newState);

        return j;
    }

    private void handleDefaultToken(NFA nfa, List<State> currentStates, String token) {
        State newState = nfa.addState();
        for (State currentState : currentStates) {
            currentState.addTransition(newState, String.valueOf(token.charAt(0)));
        }
        currentStates.clear();
        currentStates.add(newState);
    }

    private static class NFA {
        private final State startState;
        private final State endState;
        private final List<State> states;

        public NFA() {
            startState = new State();
            endState = new State();
            states = new ArrayList<>();
            states.add(startState);
            states.add(null);
        }

        public State getStartState() {
            return startState;
        }

        public State getEndState() {
            return endState;
        }

        public State addState() {
            State state = new State();
            states.add(state);
            return state;
        }

        public boolean match(String input) {
            List<State> currentStates = new ArrayList<>();
            List<State> nextStates = new ArrayList<>();
            try {
                endState.setAccepting(true);
                currentStates.add(startState);
                epsilonClosure(currentStates);
                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    nextStates.clear();
                    for (State currentState : currentStates) {
                        nextStates.addAll(currentState.getTransitions(c));
                    }
                    epsilonClosure(nextStates);
                    currentStates.clear();
                    currentStates.addAll(nextStates);
                }
                return hasAcceptingState(currentStates);
            } finally {
                endState.setAccepting(false);
            }
        }

        private boolean hasAcceptingState(List<State> states) {
            for (State currentState : states) {
                if (currentState.isAccepting()) {
                    return true;
                }
            }
            return false;
        }

        private void epsilonClosure(List<State> states) {
            List<State> queue = new ArrayList<>(states);
            while (!queue.isEmpty()) {
                State state = queue.remove(0);
                List<State> epsilonTransitions = getEpsilonTransitions(state);
                addNewStates(states, queue, epsilonTransitions);
            }
        }

        private List<State> getEpsilonTransitions(State state) {
            return state.getTransitions(null);
        }

        private void addNewStates(List<State> states, List<State> queue, List<State> newStates) {
            if (newStates != null) {
                for (State newState : newStates) {
                    if (!states.contains(newState)) {
                        states.add(newState);
                        queue.add(newState);
                    }
                }
            }
        }
    }

    /**
     * Represents a state in a non-deterministic finite automaton (NFA).
     */
    private static class State {
        private final List<Transition> transitions;
        private boolean accepting;

        /**
         * Constructs a new state with an empty list of transitions and a non-accepting
         * state.
         */
        public State() {
            transitions = new ArrayList<>();
            accepting = false;
        }

        /**
         * Adds a new transition from this state to the given next state with the given
         * input.
         *
         * @param nextState the next state in the transition
         * @param input     the input that triggers the transition
         */
        public void addTransition(State nextState, String input) {
            transitions.add(new Transition(nextState, input));
        }

        /**
         * Returns the list of next states that can be reached from this state via the
         * given input.
         *
         * @param input the input to match against the transitions
         * @return the list of next states that can be reached from this state via the
         *         given input
         */
        public List<State> getTransitions(Character input) {
            List<Transition> matchingTransitions = getMatchingTransitions(input);
            return getNextStates(matchingTransitions);
        }

        /**
         * Determines whether this state is an accepting state.
         *
         * @return true if this state is an accepting state, false otherwise
         */
        public boolean isAccepting() {
            return accepting;
        }

        /**
         * Sets whether this state is an accepting state.
         *
         * @param accepting true if this state is an accepting state, false otherwise
         */
        public void setAccepting(boolean accepting) {
            this.accepting = accepting;
        }

        /**
         * Returns the list of transitions that match the given input.
         *
         * @param input the input to match against the transitions
         * @return the list of transitions that match the given input
         */
        private List<Transition> getMatchingTransitions(Character input) {
            List<Transition> matchingTransitions = new ArrayList<>();
            for (Transition transition : transitions) {
                if (input == null || transition.input == null || transition.input.equals(input.toString())) {
                    matchingTransitions.add(transition);
                }
            }
            return matchingTransitions;
        }

        /**
         * Returns the list of next states for the given list of transitions.
         *
         * @param transitions the list of transitions to get the next states of
         * @return the list of next states for the given list of transitions
         */
        private List<State> getNextStates(List<Transition> transitions) {
            List<State> nextStates = new ArrayList<>();
            for (Transition transition : transitions) {
                nextStates.add(transition.nextState);
            }
            return (nextStates.isEmpty()) ? null : nextStates;
        }
    }

    private record Transition(State nextState, String input) {
    }

    // private boolean isSpecialCharacter(char c) {
    //     return c == '(' || c == ')' || c == '|' || c == '*' || c == '+' || c == '?'
    //             || c == '^' || c == '$';
    // }
}