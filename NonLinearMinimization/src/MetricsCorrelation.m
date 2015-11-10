function correlation=MetricsCorrelation(N,S,R,E,w)
%%function to measure the correlation between atypicality and novely
c=corrcoef(N,((S*w(1)+R*w(2)+E*w(3))/(w(1)+w(2)+w(3))));
correlation=c(2)^2;