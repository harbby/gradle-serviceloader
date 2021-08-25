package test;

public class TestImpl implements TestInterface {
    @Override
    public String getName() {
        return "hello gradle!";
    }
}
