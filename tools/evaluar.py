#!/usr/bin/env python3
"""Ejecución local y CI idéntica; elimina reportes anteriores y conserva diagnósticos."""
import os
from pathlib import Path
import re
import shutil
import subprocess
from calificar import cargar_rubrica, generar

ROOT=Path(__file__).resolve().parents[1]

def main():
    # Limpieza antes de arrancar Gradle: evita acreditar resultados de una ejecución anterior.
    for rel in ('build/test-results', 'build/reports/tests', 'build/evaluacion'):
        path=ROOT/rel
        if path.is_symlink(): path.unlink()
        elif path.exists(): shutil.rmtree(path)
    out=ROOT/'build/evaluacion';out.mkdir(parents=True,exist_ok=True)
    wrapper=['cmd','/c',str(ROOT/'gradlew.bat')] if os.name=='nt' else ['sh',str(ROOT/'gradlew')]
    estado='ok'
    log=[]
    pasos=[['--version'], ['classes','testClasses'], ['test','-Pgrading','--rerun-tasks']]
    try:
        for paso in pasos:
            cmd=wrapper+['--no-daemon','--console=plain']+paso
            print('Ejecutando:', ' '.join(paso),flush=True)
            result=subprocess.run(cmd,cwd=ROOT,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=600)
            print(result.stdout,flush=True);log.append(result.stdout)
            if result.returncode:
                if re.search(r'\.java:\d+: error:', result.stdout): estado='compilacion'
                else: estado='infraestructura' if paso[0]!='test' else 'ejecucion'
                break
    except (OSError,subprocess.TimeoutExpired) as e:
        estado='infraestructura';log.append(str(e));print(e)
    (out/'ejecucion.log').write_text('\n'.join(log),encoding='utf-8')
    (out/'estado.txt').write_text(estado+'\n',encoding='utf-8')
    codigo=generar(cargar_rubrica(ROOT/'tools/rubrica.json'),ROOT/'build/test-results/test',out/'reporte-calificacion.md',estado)
    return codigo if estado=='ok' else 2

if __name__=='__main__': raise SystemExit(main())
