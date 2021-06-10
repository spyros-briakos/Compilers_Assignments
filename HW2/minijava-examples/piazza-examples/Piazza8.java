class TestAck {
  public static void main(String[] args) {
    Ack ack;
    ack = new Ack();
    System.out.println(ack.compute(4,2));
  }
}

class Ack {
  public int compute(int m, int n) {
    int ret;

    if (!(m < 0) && !(0 < m))
      ret = n + 1;
    else if (!(n < 0) && !(0 < n))
      ret = this.compute(m - 1, 1);
    else
      ret = this.compute(m - 1, this.compute(m, n - 1));

    return ret;
  }
}