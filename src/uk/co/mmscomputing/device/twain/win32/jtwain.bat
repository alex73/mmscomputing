rem Need to set path in autoexec.bat
rem set path=%path%;J:\Borland\Bcc55\Bin 
rem Control Panel -> System -> Advanced Tab -> Environment Variables
cd J:\public_html\java\uk\co\mmscomputing\device\twain\win32
bcc32 -w-par -tWD -I"J:\Borland\Bcc55\include" -L"J:\Borland\Bcc55\lib;J:\Borland\Bcc55\lib\psdk" -L"J:\Borland\Bcc55\lib" -e"jtwain.dll" *.cpp