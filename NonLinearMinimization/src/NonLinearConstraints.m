function [c, ceq] = NonLinearConstraints(w) 
c = 0.000000001-(w(1)+w(2)+w(3))^2; 
ceq = [];  