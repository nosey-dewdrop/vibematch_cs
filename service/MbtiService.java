package service;

import model.MbtiResult;

import java.util.ArrayList;

/*
 * The vibe test. Classic MBTI style: 16 questions, 4 for each of the 4 axes
 * (E/I, S/N, T/F, J/P). Each question has two options. Option A always scores
 * the "first" letter of that axis (E, S, T or J), option B scores the second.
 *
 * After counting we pick the letter with more votes. On a 2-2 tie we lean to
 * I / N / F / P (just had to pick a side).
 */
public class MbtiService {

    // a single question
    public static class Question {
        public String text;
        public String optionA;  // scores first pole
        public String optionB;  // scores second pole
        public int axis;        // 0=E/I, 1=S/N, 2=T/F, 3=J/P

        public Question(String text, String optionA, String optionB, int axis) {
            this.text = text;
            this.optionA = optionA;
            this.optionB = optionB;
            this.axis = axis;
        }
    }

    public ArrayList<Question> getQuestions() {
        ArrayList<Question> q = new ArrayList<Question>();

        // axis 0 : Extraversion vs Introversion
        q.add(new Question("After a long week, you recharge by...",
                "Going out with a group of friends", "Having a quiet night to yourself", 0));
        q.add(new Question("At a party you don't know many people, you...",
                "Start chatting with strangers", "Stick with the one person you know", 0));
        q.add(new Question("You get your best energy from...",
                "Being around lots of people", "Smaller, calmer settings", 0));
        q.add(new Question("Group projects feel...",
                "Fun, you like bouncing ideas out loud", "Tiring, you'd rather work your part alone", 0));

        // axis 1 : Sensing vs iNtuition
        q.add(new Question("You trust...",
                "Facts and what you can see right now", "Patterns and where things could go", 1));
        q.add(new Question("When learning something new you prefer...",
                "Concrete steps and examples", "The big picture and the theory", 1));
        q.add(new Question("You'd describe yourself as more...",
                "Practical and down to earth", "Imaginative and a bit dreamy", 1));
        q.add(new Question("You notice...",
                "The details others miss", "The meaning behind things", 1));

        // axis 2 : Thinking vs Feeling
        q.add(new Question("When making a decision you go with...",
                "Logic, even if it's a bit cold", "How people will feel about it", 2));
        q.add(new Question("A friend is upset, you first...",
                "Try to solve the problem", "Make sure they feel heard", 2));
        q.add(new Question("You'd rather be seen as...",
                "Fair and consistent", "Warm and caring", 2));
        q.add(new Question("In an argument you focus on...",
                "Who is actually right", "Keeping the peace", 2));

        // axis 3 : Judging vs Perceiving
        q.add(new Question("Your ideal weekend is...",
                "Planned out in advance", "Open, see what happens", 3));
        q.add(new Question("Your desk / room is usually...",
                "Tidy and organized", "A creative kind of mess", 3));
        q.add(new Question("Deadlines make you...",
                "Start early and finish ahead", "Do your best work last minute", 3));
        q.add(new Question("You feel better when things are...",
                "Decided and settled", "Still open to change", 3));

        return q;
    }

    /*
     * answers[i] is 0 if they picked option A, 1 if option B. Must be 16 long.
     */
    public MbtiResult score(int[] answers) {
        ArrayList<Question> questions = getQuestions();

        // count how many times the first pole was chosen, per axis
        int[] firstPole = new int[4];
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (answers[i] == 0) {
                firstPole[q.axis] = firstPole[q.axis] + 1;
            }
        }

        // 4 questions per axis, so > 2 means the first letter wins
        String letterEI = firstPole[0] > 2 ? "E" : "I";
        String letterSN = firstPole[1] > 2 ? "S" : "N";
        String letterTF = firstPole[2] > 2 ? "T" : "F";
        String letterJP = firstPole[3] > 2 ? "J" : "P";

        String type = letterEI + letterSN + letterTF + letterJP;

        int eiPct = firstPole[0] * 25; // 0..4 -> 0..100
        int snPct = firstPole[1] * 25;
        int tfPct = firstPole[2] * 25;
        int jpPct = firstPole[3] * 25;

        return new MbtiResult(type, eiPct, snPct, tfPct, jpPct);
    }

    // ---- archetypes, just a friendlier label for each type ----

    public String archetypeEmoji(String type) {
        if (type.equals("INTJ")) return "🦉";
        if (type.equals("INTP")) return "🔭";
        if (type.equals("ENTJ")) return "🎯";
        if (type.equals("ENTP")) return "⚡";
        if (type.equals("INFJ")) return "🌙";
        if (type.equals("INFP")) return "🌿";
        if (type.equals("ENFJ")) return "🌟";
        if (type.equals("ENFP")) return "🎈";
        if (type.equals("ISTJ")) return "📚";
        if (type.equals("ISFJ")) return "🤍";
        if (type.equals("ESTJ")) return "🧭";
        if (type.equals("ESFJ")) return "☕";
        if (type.equals("ISTP")) return "🛠️";
        if (type.equals("ISFP")) return "🎨";
        if (type.equals("ESTP")) return "🔥";
        if (type.equals("ESFP")) return "🎉";
        return "✨";
    }

    public String archetypeName(String type) {
        if (type.equals("INTJ")) return "The Mastermind";
        if (type.equals("INTP")) return "The Theorist";
        if (type.equals("ENTJ")) return "The Commander";
        if (type.equals("ENTP")) return "The Spark";
        if (type.equals("INFJ")) return "The Idealist";
        if (type.equals("INFP")) return "The Dreamer";
        if (type.equals("ENFJ")) return "The Mentor";
        if (type.equals("ENFP")) return "The Free Spirit";
        if (type.equals("ISTJ")) return "The Planner";
        if (type.equals("ISFJ")) return "The Caretaker";
        if (type.equals("ESTJ")) return "The Organizer";
        if (type.equals("ESFJ")) return "The Host";
        if (type.equals("ISTP")) return "The Tinkerer";
        if (type.equals("ISFP")) return "The Artist";
        if (type.equals("ESTP")) return "The Dynamo";
        if (type.equals("ESFP")) return "The Entertainer";
        return "The Original";
    }

    public String archetypeBlurb(String type) {
        if (type.equals("INTJ")) return "Strategic and independent, you like a plan and the long game.";
        if (type.equals("INTP")) return "Endlessly curious, you'd happily fall down any rabbit hole of ideas.";
        if (type.equals("ENTJ")) return "A natural organizer who likes to get people moving toward a goal.";
        if (type.equals("ENTP")) return "Quick witted and playful, you love a good debate and a new idea.";
        if (type.equals("INFJ")) return "Quietly visionary, you care about meaning and the people around you.";
        if (type.equals("INFP")) return "Gentle and values driven, you feel things deeply and dream big.";
        if (type.equals("ENFJ")) return "Warm and encouraging, you bring people together like glue.";
        if (type.equals("ENFP")) return "Bubbly and curious, you collect people and ideas wherever you go.";
        if (type.equals("ISTJ")) return "Reliable and grounded, when you say you'll do it, it gets done.";
        if (type.equals("ISFJ")) return "Kind and loyal, you quietly look out for everyone around you.";
        if (type.equals("ESTJ")) return "Practical and decisive, you like things running smoothly.";
        if (type.equals("ESFJ")) return "Friendly and caring, you're the heart of any group.";
        if (type.equals("ISTP")) return "Calm and hands on, you like figuring out how things work.";
        if (type.equals("ISFP")) return "Soft spoken and creative, you live in the little beautiful moments.";
        if (type.equals("ESTP")) return "Bold and full of energy, you jump in and figure it out as you go.";
        if (type.equals("ESFP")) return "Fun and spontaneous, you turn ordinary days into something lively.";
        return "One of a kind, you don't fit neatly in a box.";
    }
}
