    public static boolean isBlank(CharSequence str) {
        int length = str.length();
        if (length == 0) {
            return true;
        }
        char ch;
        for (int i = 0; i < length; i++) {
            ch = str.charAt(i);
            if (ch != ' ') {
                return false;
            }
        }
        return true;
    }