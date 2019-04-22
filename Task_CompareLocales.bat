
cd build\distributions\nodecore-wallet-ui\bin

call nodecore-wallet-ui.bat locale merge test zh_cn ..\..\..\..\src\main\resources\locale
call nodecore-wallet-ui.bat locale merge test en_us ..\..\..\..\src\main\resources\locale
call nodecore-wallet-ui.bat locale merge test it_it ..\..\..\..\src\main\resources\locale
call nodecore-wallet-ui.bat locale merge test ro_ro ..\..\..\..\src\main\resources\locale
call nodecore-wallet-ui.bat locale merge test hi_in ..\..\..\..\src\main\resources\locale
echo Done with all
Pause
