function [w,value]=NonLinearMinimization(N,S,R,E) 
%function to find the optimum value of w to orthogonalize novelty and atypicality 
initial_w=ones(1,3);
%lower and upper bounds for each variable w
LowBounds=zeros(1,3);
UpBounds=[1000,1000,1000];
%function to be minimized
ObjectiveFun=@(w) MetricsCorrelation(N,S,R,E,w);
%function to define the non linear constraints
ConstraintFun=@(w) NonLinearConstraints(w);
options = optimoptions('fmincon','Algorithm','interior-point');
[w,value]=fmincon(ObjectiveFun,initial_w,[],[],[],[],LowBounds,UpBounds,ConstraintFun,options);%fmincon
