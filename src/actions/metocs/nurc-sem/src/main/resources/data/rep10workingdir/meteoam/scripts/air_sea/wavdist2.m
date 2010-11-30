function y=wavdist2(za)
% WAVDIST2: plots wave distortion effects on wind at za.
% WAVDIST2(za) plots the effects of wave distortion on the 
% wind Ua measured at height za for the following significant 
% wave heights Hw=[0:2:8] in m.
%
% INPUT:   za - wind measurement height [m]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 5/5/97: version 1.0
% 4/10/98: version 1.1
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

Hw=[0:2:8];
Ua=[0.01:.01:20]';

N=length(Hw);
M=length(Ua);
Ut=zeros(M,N);

clg
hold on
for n=1:N
  Ut=wavdist1(Ua,za,Hw(n));
  plot(Ua,Ut)
end

title(['Predicted effects of wave distortion on wind speed at height ',num2str(za),' m'])
xlabel('Measured wind speed Ua (m/s)')
ylabel('Predicted wind speed Ut (m/s)')
text(10,2,'Wave height increment = 2 m')
grid


