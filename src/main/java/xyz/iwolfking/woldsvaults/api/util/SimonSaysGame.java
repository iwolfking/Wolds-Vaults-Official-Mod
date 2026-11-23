package xyz.iwolfking.woldsvaults.api.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The game of Simon Says on its own, for anything in the pack that wants a player to repeat a pattern back. Pieces can
 * be anything (entities, blocks, buttons) and nothing here touches Minecraft: the game only keeps the rules and says
 * what happened, and whoever runs it decides what lighting up or a wrong answer actually does.
 * A round is begun with {@link #start}. Call {@link #tick} once a game tick and act on its {@link Cue}: pieces light up
 * and go dark one at a time, then it's the player's turn, against the clock. Call {@link #press} when they pick a piece
 * and act on its {@link Press}. Some pieces can be wild: one picked out of turn stands in for the step that's due, once
 * a round. A {@link Streak} kept between rounds makes each one a step longer than the last.
 * Simon's Set ({@link xyz.iwolfking.woldsvaults.modifiers.gear.simon.SimonsSet}) plays it with its crystals.
 */
public final class SimonSaysGame<T> {
    // how a round is paced, in ticks: the pause before the first step, how long a step stays lit and the gap after it,
    // and how long the player gets to answer (a base plus some per step)
    public record Timing(int leadTicks, int showTicks, int gapTicks, int answerBaseTicks, int answerTicksPerStep) {
    }

    public enum CueKind {
        NONE,
        // this piece is the step being shown
        LIGHT,
        // this piece's time being shown is over
        DIM,
        // the whole pattern has been shown and the clock has started
        PLAYER_TURN,
        // the player ran out of time, which loses the round
        TIMED_OUT
    }

    // what a tick asks for, and the piece it's about for LIGHT and DIM
    public record Cue<T>(CueKind kind, @Nullable T piece) {
    }

    public enum Outcome {
        // not the player's turn, or not a piece in this round: nothing happened
        IGNORED,
        // the step that was due
        CORRECT,
        // a wild piece picked out of turn: the step that was due counts as done
        STAND_IN,
        // the wrong piece, which loses the round
        WRONG
    }

    // What came of a press: the step that was due, whether that step's piece has no more turns this round, and whether
    // this finished the pattern. For STAND_IN the step isn't the piece that was pressed.
    public record Press<T>(Outcome outcome, @Nullable T step, boolean lastTurn, boolean finished) {
    }

    // a run of rounds won in a row, for whoever runs the game to keep between them and forget when one is lost
    public static final class Streak {
        private int wins;
        private int lastLength;

        public int wins() {
            return this.wins;
        }

        public int lastLength() {
            return this.lastLength;
        }

        // how long the next round should be: every piece at least once, and a step longer than the last round won
        public int lengthFor(int pieces) {
            return Math.max(pieces, this.wins > 0 ? this.lastLength + 1 : 0);
        }

        public void won(int length) {
            this.wins++;
            this.lastLength = length;
        }
    }

    private final List<T> steps;
    private final List<T> pieces;
    private final Timing timing;
    private final Predicate<? super T> wild;
    private final Set<T> wildUsed = new HashSet<>();
    private int age;
    private int next;
    private boolean playerTurn;
    private int timeLeft;

    private SimonSaysGame(List<T> steps, List<T> pieces, Timing timing, Predicate<? super T> wild) {
        this.steps = steps;
        this.pieces = pieces;
        this.timing = timing;
        this.wild = wild;
    }

    // A new round: every piece once in the order given, then at random, never the same one twice running, until the
    // pattern is `length` steps long.
    public static <T> SimonSaysGame<T> start(List<T> pieces, int length, Timing timing, Predicate<? super T> wild, Random random) {
        if (pieces.isEmpty()) {
            throw new IllegalArgumentException("A game of Simon Says needs at least one piece");
        }
        List<T> members = new ArrayList<>(pieces);
        List<T> steps = new ArrayList<>(members);
        while (steps.size() < length) {
            T previous = steps.get(steps.size() - 1);
            List<T> choices = members.size() > 1 ? members.stream().filter(piece -> !piece.equals(previous)).toList() : members;
            steps.add(choices.get(random.nextInt(choices.size())));
        }
        return new SimonSaysGame<>(steps, members, timing, wild);
    }

    // One game tick: what's being shown, or the player's clock running down.
    public Cue<T> tick() {
        this.age++;
        if (this.playerTurn) {
            return new Cue<>(--this.timeLeft <= 0 ? CueKind.TIMED_OUT : CueKind.NONE, null);
        }
        int shown = this.age - this.timing.leadTicks();
        if (shown < 0) {
            return new Cue<>(CueKind.NONE, null);
        }
        int ticksPerStep = Math.max(1, this.timing.showTicks() + this.timing.gapTicks());
        int index = shown / ticksPerStep;
        if (index >= this.steps.size()) {
            this.playerTurn = true;
            this.timeLeft = Math.max(1, this.timing.answerBaseTicks() + this.timing.answerTicksPerStep() * this.steps.size());
            return new Cue<>(CueKind.PLAYER_TURN, null);
        }
        int within = shown % ticksPerStep;
        if (within == 0) {
            return new Cue<>(CueKind.LIGHT, this.steps.get(index));
        }
        return within == this.timing.showTicks() ? new Cue<>(CueKind.DIM, this.steps.get(index)) : new Cue<>(CueKind.NONE, null);
    }

    // The player picks a piece.
    public Press<T> press(T piece) {
        if (!this.playerTurn || this.next >= this.steps.size()) {
            return new Press<>(Outcome.IGNORED, null, false, false);
        }
        T due = this.steps.get(this.next);
        Outcome outcome;
        if (piece.equals(due)) {
            outcome = Outcome.CORRECT;
        } else if (this.wild.test(piece) && this.pieces.contains(piece) && this.wildUsed.add(piece)) {
            outcome = Outcome.STAND_IN;
        } else if (this.pieces.contains(piece)) {
            return new Press<>(Outcome.WRONG, due, false, false);
        } else {
            return new Press<>(Outcome.IGNORED, null, false, false);
        }
        this.next++;
        boolean lastTurn = !this.steps.subList(this.next, this.steps.size()).contains(due);
        return new Press<>(outcome, due, lastTurn, this.next >= this.steps.size());
    }

    public boolean isPlayerTurn() {
        return this.playerTurn;
    }

    // how many steps the pattern has
    public int length() {
        return this.steps.size();
    }

    // the pieces still in the round, each once
    public List<T> pieces() {
        return Collections.unmodifiableList(this.pieces);
    }

    public boolean includes(T piece) {
        return this.pieces.contains(piece);
    }

    // a piece that's used up and no longer counts as being in the round
    public void remove(T piece) {
        this.pieces.remove(piece);
    }
}
