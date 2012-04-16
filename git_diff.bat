echo iniciando...%3
cd %3
git diff %1 %2 --name-status
echo finalizando...