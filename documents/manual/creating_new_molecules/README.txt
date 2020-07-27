Creating New Molecules 
=======


-----------------------
itp-format:
Add a list of all atoms of your molecule between [ atoms ] and [ bonds ] and between [ bonds ] and [ pairs ] add a list of all bonds between the atoms ai and aj, with any i and j between [1:n]. For each atom, you may add characteristics in their dedicated columns. You may use https://github.com/GaBil100/eXamine-eXamol-/blob/stand-alone/data/molecule/test.itp as a template. 

[ atoms ]
;  nr  type  resnr  resid  atom  cgnr  charge    mass    total_charge
    1    C    1    _a    C12    1    0.123   12.0110      ;1234
    .                     .
    .                     .
    .                     .
    n                     .
; total charge of the molecule:   0.000
[ bonds ]
;  ai   aj  funct   c0         c1
    1    2   .      .           .
    .    .
    .    .  
[ pairs ]

;NAN
