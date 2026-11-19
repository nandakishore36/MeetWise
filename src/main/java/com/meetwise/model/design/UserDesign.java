package com.meetwise.model.design;

import com.meetwise.model.Data.User;

public interface UserDesign {

    boolean registerUser(User user, int adminId);

    User loginUser(String email, String password);

    User getUserById(int userId);

}
