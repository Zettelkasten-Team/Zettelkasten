package ch.unibe.jexample;

import static org.junit.Assert.assertEquals;
import java.util.Stack;
import org.junit.runner.RunWith;
import org.junit.Test;
import ch.unibe.jexample.JExample;  // Ensure JExample is correctly imported

@RunWith(JExample.class)
public class ExampleStack {

    @Test
    public Stack whenEmpty() {
        Stack<String> stack = new Stack<>();
        assertEquals(true, stack.isEmpty());
        return stack;
    }

    @Given("#whenEmpty")
    public Stack<String> shouldPush(Stack<String> stack) {
        stack.push("Foo");
        assertEquals(false, stack.isEmpty());
        assertEquals("Foo", stack.peek());  // Use peek() instead of top()
        return stack;
    }

    @Given("shouldPush")
    public void shouldPop(Stack<String> stack) {
        Object top = stack.pop();
        assertEquals(true, stack.isEmpty());
        assertEquals("Foo", top);
    }
}
