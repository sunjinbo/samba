package com.samba;

import android.text.TextUtils;

public class SambaUtil {
    /**
     * For example, if path is xxx/yyy/zzz/AAA.bbb<p/>
     * It'll return AAA.bbb
     */
    public final static String getFileName(String path) {
        if (path == null) {
            return path;
        }
        if (!path.contains("/")) {
            return path;
        }
        int index = path.lastIndexOf("/");
        if (index < 0 || index + 1 >= path.length()) {
            return path;
        }
        return path.substring(index + 1);
    }

    public final static String getParentPath(String path) {
        if (path == null) {
            return path;
        }
        if (!path.contains("/")) {
            return path;
        }
        int index = path.lastIndexOf("/");
        if (index < 0 || index + 1 >= path.length()) {
            return path;
        }
        return path.substring(0,index);
    }

    /**
     * For example, if path is xxx/yyy/zzz/AAA.bbb<p/>
     * It'll return AAA
     */
    public final static String getNakedName(String path) {
        path = getFileName(path);
        if (path == null) {
            return path;
        }
        if (!path.contains(".")) {
            return path;
        }
        int index = path.lastIndexOf(".");
        if (index <= 0 || index + 1 >= path.length()) {
            return null;
        }
        return path.substring(0, index);
    }

    public final static String autoRename(String name) {
        String nakedName = getNakedName(name);
        String suffix = name.replace(nakedName, "");
        return new StringBuilder(nakedName).append("-").append(System.currentTimeMillis()).append(suffix).toString();
    }

    public final static String strsToString(String[] strs) {
        if (strs == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(strs.length);
        for (String str : strs) {
            builder.append("\n");
            builder.append("[");
            builder.append(str);
            builder.append("]");
        }
        return builder.toString();
    }

    public final static String wrapSmbFileUrl(String parent, String name) {
        if (TextUtils.isEmpty(parent) || TextUtils.isEmpty(name)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(parent);
        if (!parent.endsWith("/")) {
            builder.append("/");
        }
        if (name.endsWith("/")) {
            int index = name.length() - 1;
            name = name.substring(0, index - 1);
        }
        builder.append(name);
        return builder.toString();
    }

}
