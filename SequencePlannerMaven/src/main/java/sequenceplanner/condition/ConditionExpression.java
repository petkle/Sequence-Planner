
package sequenceplanner.condition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author kbe
 */
public class ConditionExpression extends ConditionElement implements Iterable<ConditionElement> {

    private ConditionElement expressionRoot = null;

    public ConditionExpression(){
        super();
        expressionRoot = null;
    }

    public ConditionExpression(ConditionElement expressionRoot){
        super();
        if (validateExpression(expressionRoot)){
            this.expressionRoot = expressionRoot;
        }
        this.expressionRoot = null;
    }

    public ConditionExpression(ConditionElement expressionRoot,
                               ConditionOperator previousOperator,
                               ConditionOperator nextOperator)
    {
        super(previousOperator, nextOperator);
        this.expressionRoot = expressionRoot;
    }

    public Iterator<ConditionElement> iterator() {
        return new ConditionElementIterator(expressionRoot);
    }

    public List<ConditionElement> getConditionElements(){
        return getFollowingElements(expressionRoot);
    }

    public boolean isEmpty(){
        return this.expressionRoot == null;
    }

    public void changeExpressionRoot(ConditionElement root){
        if (validateExpression(root)){
            this.expressionRoot = root;
        } else this.expressionRoot = null;
    }

    public ConditionElement getExpressionRoot(){
        return this.expressionRoot;
    }

    public boolean appendElement(ConditionOperator co, ConditionElement ce){
        if (ce == null) return false;
        ConditionElement last = getLastElement();
        if (last == null){
            this.changeExpressionRoot(ce);
            return expressionRoot!= null;
        }
        connectElements(last, co, ce);
        if (validateExpression(expressionRoot)){
            return true;
        } else {
            disConnectElements(last, ce);
            return false;
        }
    }

    public boolean appendElement(ConditionOperator.Type t, ConditionElement ce){
        return appendElement(new ConditionOperator(t), ce);
    }
    

    private boolean validateExpression(ConditionElement expressionRoot){
        if (expressionRoot == null) return false;
        if (expressionRoot.getPreviousOperator() != null) return false;

        ConditionElement current = expressionRoot;
        while (current.hasNextOperator()){
            ConditionOperator co = current.getNextOperator();
            if (!co.getPreviousElement().equals(current)) return false;
            if (!co.hasNextElement()) return false;
            current = co.getNextElement();
            if (!current.getPreviousOperator().equals(co)) return false;
        }

        return true;
    }

    public boolean containsElement(ConditionElement ce){
        if (ce == null) return false;
        for (ConditionElement e : this){
            if (e.isExpression()){
                ConditionExpression expr = (ConditionExpression) e;
                if (expr.containsElement(ce)){
                    return true;
                }
            } else if (e.isStatment()){
                if (e.equals(ce)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }





    private void connectElements(ConditionElement from, ConditionOperator op, ConditionElement to){
        from.setNextOperator(op);
        op.setPreviousElement(from);
        op.setNextElement(to);
        to.setPreviousOperator(op);
    }

    /**
     * Will disconnect two elements from - operator - to, if the are connected,
     * returns false otherwise
     * @return true if suceeded, false if they wasn't connected.
     */
    private boolean disConnectElements(ConditionElement from, ConditionElement to){
        if (!isConnected(from, to)) return false;
        ConditionOperator co = from.getNextOperator();
        from.setNextOperator(null);
        co.clear();
        to.setPreviousOperator(null);
        return true;
    }

    private boolean isConnected(ConditionElement from, ConditionElement to){
        if (from.getNextElement().equals(to)
            && to.getPreviousElement().equals(from)
            && from.getNextOperator().equals(to.getPreviousOperator()))
        {
            return true;
        } else {
            return false;
        }
    }

    private ConditionElement getLastElement(){
        if (!validateExpression(expressionRoot)) return null;
        ConditionElement current = this.expressionRoot;
        while (current.hasNextElement()){
            current = current.getNextElement();
        }
        return current;
    }

    private List<ConditionElement> getFollowingElements(ConditionElement from){
        List<ConditionElement> list = new ArrayList<ConditionElement>();
        if (from == null) return list;
        if (validateExpression(from)) return list;

        list.add(from);
        ConditionElement current = from;
        while (current.hasNextElement()){
            current = current.getNextElement();
            list.add(current);
        }
        return list;
    }

    private List<ConditionElement> getPreviousElements(ConditionElement from){
        List<ConditionElement> list = new ArrayList<ConditionElement>();
        if (from == null) return list;
        if (validateExpression(from)) return list;

        list.add(from);
        ConditionElement current = from;
        while (current.hasPreviousElement()){
            current = current.getPreviousElement();
            list.add(current);
        }
        return list;
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    @Override
    public boolean isStatment() {
        return false;
    }

    @Override
    public String toString(){
        String result = "(";
        for (ConditionElement e : this){
            result += e;
        }
        return result + ")";
    }







    class ConditionElementIterator implements Iterator<ConditionElement>{
        ConditionElement next = null;

        public ConditionElementIterator(ConditionElement rootElement){
            this.next = rootElement;
        }

        public boolean hasNext() {
            return next != null;
        }

        public ConditionElement next() {
            if (hasNext()){
                ConditionElement ce = next;
                next = next.getNextElement();
                return ce;
            }
            throw new NoSuchElementException("No more ConditionElements");
        }

        // This method is a little bit scarry. Maybe we should do nothing?
        public void remove() {
            ConditionElement current = next;
            if (current != null){
                if (current.hasNextElement() && current.hasPreviousElement()) {
                    ConditionElement p = current.getPreviousElement();
                    ConditionElement n = current.getNextElement();
                    ConditionOperator co;
                    // Not sure about this but here OR is used if any of the operatores are or.
                    if (current.getNextOperator().isOperationType(ConditionOperator.Type.OR)
                            || current.getPreviousOperator().isOperationType(ConditionOperator.Type.OR))
                    {
                        co = new ConditionOperator(p, n, ConditionOperator.Type.OR);
                    } else {
                        co = new ConditionOperator(p, n, ConditionOperator.Type.AND);
                    }
                    p.setNextOperator(co);
                    n.setPreviousOperator(co);
                    next = p;
                } else if (current.hasNextElement()) {
                    ConditionElement n = current.getNextElement();
                    n.setPreviousOperator(null);
                    current.getNextOperator().clear();
                    current.clear();
                    next = n;
                } else if (current.hasPreviousElement()) {
                    ConditionElement prev = current.getPreviousElement();
                    prev.setNextOperator(null);
                    current.getPreviousOperator().clear();
                    current.clear();
                    next = prev;
                } else {
                    current.clear();
                    next = null;
                }
            }
        }

    }

}
