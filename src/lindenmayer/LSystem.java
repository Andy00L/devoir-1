package lindenmayer;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class LSystem extends AbstractLSystem {

    private Map<Character, Symbol> symbolMap;
    private Map<Symbol, String> actionMap;
    private Map<Symbol, List<List<Symbol>>> rulesMap;
    private List<Symbol> axiom;

    public LSystem() {
        super();
        symbolMap = new HashMap<>();
        actionMap = new HashMap<>();
        rulesMap = new HashMap<>();
        axiom = new ArrayList<>();
    }

    @Override
    public Symbol setAction(char sym, String action) {
        Symbol symbol = symbolMap.get(sym);
        if (symbol == null) {
            symbol = new Symbol(sym);
            symbolMap.put(sym, symbol);
        }
        actionMap.put(symbol, action);
        return symbol;
    }

    @Override
    public void setAxiom(String str) {
        axiom.clear();
        for (char c : str.toCharArray()) {
            Symbol sym = symbolMap.get(c);
            if (sym == null) {
                // Créer le symbole s'il n'existe pas
                sym = new Symbol(c);
                symbolMap.put(c, sym);
            }
            axiom.add(sym);
        }
    }

    @Override
    public void addRule(char sym, String expansion) {
        Symbol symbol = symbolMap.get(sym);
        if (symbol == null) {
            symbol = new Symbol(sym);
            symbolMap.put(sym, symbol);
        }

        List<Symbol> expansionList = new ArrayList<>();
        for (char c : expansion.toCharArray()) {
            Symbol expSym = symbolMap.get(c);
            if (expSym == null) {
                expSym = new Symbol(c);
                symbolMap.put(c, expSym);
            }
            expansionList.add(expSym);
        }

        rulesMap.computeIfAbsent(symbol, k -> new ArrayList<>()).add(expansionList);
    }

    @Override
    public Iterator<Symbol> getAxiom() {
        return axiom.iterator();
    }

    @Override
    public Iterator<Symbol> rewrite(Symbol sym) {
        List<List<Symbol>> rules = rulesMap.get(sym);
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        List<Symbol> chosen = rndElement(rules);
        return chosen.iterator();
    }

    @Override
    public void tell(Turtle turtle, Symbol sym) {
        String action = actionMap.get(sym);
        if (action != null) {
            turtle.action(action).run();
        } else {
            // Comportement par défaut selon l'encodage standard
            char c = sym.toString().charAt(0);
            if (Character.isUpperCase(c)) {
                turtle.draw();
            } else if (Character.isLowerCase(c)) {
                turtle.move();
            }
        }
    }

    @Override
    public Rectangle2D tell(Turtle turtle, Iterator<Symbol> seq, int rounds) {
        resetRnd();
        Point2D start = turtle.getPosition();
        Rectangle2D bbox = new Rectangle2D.Double(start.getX(), start.getY(), 0, 0);
        return tellRecursive(turtle, seq, rounds, bbox);
    }

    private Rectangle2D tellRecursive(Turtle turtle, Iterator<Symbol> seq, int rounds, Rectangle2D bbox) {
        if (rounds == 0) {
            while (seq.hasNext()) {
                Symbol sym = seq.next();
                tell(turtle, sym);
                Point2D pos = turtle.getPosition();
                bbox = bbox.createUnion(new Rectangle2D.Double(pos.getX(), pos.getY(), 0, 0));
            }
        } else {
            while (seq.hasNext()) {
                Symbol sym = seq.next();
                Iterator<Symbol> expansion = rewrite(sym);
                if (expansion == null) {
                    List<Symbol> single = Arrays.asList(sym);
                    bbox = tellRecursive(turtle, single.iterator(), 0, bbox);
                } else {
                    bbox = tellRecursive(turtle, expansion, rounds - 1, bbox);
                }
            }
        }
        return bbox;
    }

    @Override
    protected void initFromJson(JSONObject obj, Turtle turtle) {
        JSONObject actions = obj.getJSONObject("actions");
        for (String key : actions.keySet()) {
            String action = actions.getString(key);
            char c = key.charAt(0);
            setAction(c, action);
        }

        String axiomStr = obj.getString("axiom");
        setAxiom(axiomStr);

        if (obj.has("rules")) {
            JSONObject rules = obj.getJSONObject("rules");
            for (String key : rules.keySet()) {
                char sym = key.charAt(0);
                Object ruleObj = rules.get(key);

                if (ruleObj instanceof JSONArray) {
                    JSONArray ruleArray = (JSONArray) ruleObj;
                    for (int i = 0; i < ruleArray.length(); i++) {
                        addRule(sym, ruleArray.getString(i));
                    }
                }
            }
        }

        if (obj.has("parameters")) {
            JSONObject params = obj.getJSONObject("parameters");
            double step = params.getDouble("step");
            double angle = params.getDouble("angle");
            turtle.setUnits(step, angle);

            if (params.has("start")) {
                JSONArray start = params.getJSONArray("start");
                double x = start.getDouble(0);
                double y = start.getDouble(1);
                double theta = start.getDouble(2);
                turtle.init(new Point2D.Double(x, y), theta);
            }
        }
    }
}