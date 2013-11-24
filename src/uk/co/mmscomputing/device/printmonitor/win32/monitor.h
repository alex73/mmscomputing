#include <windows.h>
#include "winsplp.h"         // windows ddk

BOOL WINAPI Monitor_EnumPorts    (LPWSTR servername,DWORD dwLevel,LPBYTE pPorts,DWORD cbBuf,LPDWORD pdwNeeded,LPDWORD pdwReturned);
BOOL WINAPI Monitor_OpenPort     (LPWSTR portname,PHANDLE porthandle);
BOOL WINAPI Monitor_OpenPortEx   (LPWSTR pszPortName,LPWSTR pszPrinterName,LPHANDLE pHandle,LPMONITOR pMonitor);
BOOL WINAPI Monitor_StartDocPort (HANDLE hPort,LPWSTR pPrinterName,DWORD JobId,DWORD Level,LPBYTE pDocInfo);
BOOL WINAPI Monitor_WritePort    (HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbWritten);
BOOL WINAPI Monitor_ReadPort     (HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbRead);
BOOL WINAPI Monitor_EndDocPort   (HANDLE hPort);
BOOL WINAPI Monitor_ClosePort    (HANDLE hPort);
BOOL WINAPI Monitor_AddPort      (LPWSTR servername,HWND hWnd,LPWSTR monitorname);
BOOL WINAPI Monitor_AddPortEx    (LPWSTR pName,DWORD Level,LPBYTE pBuffer,LPWSTR pMonitorName);
BOOL WINAPI Monitor_ConfigurePort(LPWSTR servername,HWND hWnd,LPWSTR portname);
BOOL WINAPI Monitor_DeletePort   (LPWSTR servername,HWND hWnd,LPWSTR portname);
BOOL WINAPI Monitor_GetPrinterDataFromPort(HANDLE hPort,DWORD ControlID,LPWSTR pValueName,LPWSTR lpInBuffer,
    DWORD cbInBuffer,LPWSTR lpOutBuffer,DWORD cbOutBuffer,LPDWORD lpcbReturned);
BOOL WINAPI Monitor_SetPortTimeOuts(HANDLE hPort,LPCOMMTIMEOUTS lpCTO,DWORD reserved);

bool Monitor_Init(LPWSTR newMonitorRegKey);