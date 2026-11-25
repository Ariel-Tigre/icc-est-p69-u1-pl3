#!/usr/bin/env python3
"""Califica IDs fijos a partir de XML JUnit. No contiene algoritmos del ejercicio."""
import argparse
from collections import Counter, defaultdict
from decimal import Decimal, ROUND_HALF_UP
import json
import os
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

def cargar_rubrica(path):
    config = json.loads(Path(path).read_text(encoding='utf-8'))
    ids = []
    grupos = set()
    for g in config['grupos']:
        if g['id'] in grupos or Decimal(str(g['maximo'])) < 0 or not g['pruebas']:
            raise ValueError('Grupo duplicado, peso negativo o sin pruebas')
        grupos.add(g['id'])
        ids.extend(g['pruebas'])
    if len(ids) != len(set(ids)):
        raise ValueError('Identificadores duplicados en la rúbrica')
    if sum(Decimal(str(g['maximo'])) for g in config['grupos']) != Decimal(str(config['maximo'])):
        raise ValueError('Las ponderaciones no suman la escala')
    if Decimal(str(config['maximo'])) <= 0:
        raise ValueError('Escala inválida')
    return config

def analizar(config, directory):
    encontrados = defaultdict(list)
    problemas = []
    for file in sorted(Path(directory).glob('TEST-*.xml')):
        try:
            root = ET.parse(file).getroot()
        except (ET.ParseError, OSError) as e:
            problemas.append(f'XML inválido: {file.name}: {e}')
            continue
        for case in root.iter('testcase'):
            nombre = case.get('name', '')
            if nombre.endswith('()'):
                nombre = nombre[:-2]
            identidad = case.get('classname', '') + '#' + nombre
            estado = 'aprobada'
            if case.find('skipped') is not None:
                estado = 'omitida'
            elif case.find('error') is not None:
                estado = 'error'
            elif case.find('failure') is not None:
                failure = case.find('failure')
                tipo = failure.get('type', '')
                estado = 'fallida' if ('Assertion' in tipo or 'opentest4j' in tipo) else 'error'
            encontrados[identidad].append(estado)
    detalles = {}
    total = Decimal(0)
    filas = []
    for g in config['grupos']:
        conteo = Counter()
        for identidad in g['pruebas']:
            estados = encontrados.get(identidad, [])
            estado = 'no ejecutada' if not estados else 'duplicada' if len(estados) != 1 else estados[0]
            detalles[identidad] = estado
            conteo[estado] += 1
        nota = Decimal(conteo['aprobada']) / Decimal(len(g['pruebas'])) * Decimal(str(g['maximo']))
        total += nota
        filas.append((g, conteo, nota))
    return filas, total, detalles, problemas, encontrados

def generar(config, directory, output, estado='ok'):
    filas, total, detalles, problemas, encontrados = analizar(config, directory)
    def numero(x): return str(x.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP))
    incompleto = any(s in ('no ejecutada', 'duplicada') for s in detalles.values())
    concluyente = estado == 'ok' and not problemas and not incompleto
    lines = ['# Resultado de icc-est-u1-pl3', '',
             f"Commit: `{os.environ.get('GITHUB_SHA', 'ejecución local')}`", '',
             '| Componente | Esperadas | Aprobadas | Fallidas | Errores | Omitidas | No ejecutadas | Duplicadas | Nota | Máximo |',
             '|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|']
    for g,c,n in filas:
        lines.append(f"| {g['nombre']} | {len(g['pruebas'])} | {c['aprobada']} | {c['fallida']} | {c['error']} | {c['omitida']} | {c['no ejecutada']} | {c['duplicada']} | {numero(n)} | {g['maximo']:.2f} |")
    etiqueta = 'Calificación de retroalimentación' if concluyente else 'Puntaje observado provisional'
    lines += ['', f"**{etiqueta}: {numero(total)} / {config['maximo']:.2f}**",
              f"Cumplimiento ponderado: {numero(total / Decimal(str(config['maximo'])) * 100)} %", '']
    if estado == 'compilacion':
        lines.append('El código no compiló. Las pruebas no ejecutadas no suman puntos. Consulte build/evaluacion/ejecucion.log.')
    elif estado != 'ok':
        lines.append(f'Evaluación no concluyente: {estado}. Un fallo de entorno o ejecución no constituye una nota académica definitiva.')
    if incompleto:
        lines.append('Hay casos ausentes o duplicados. El denominador original se mantiene y estos casos no suman puntos.')
    lines.extend(problemas)
    lines += ['', '## Casos evaluables', '', '| ID | Estado |', '|---|---|']
    for k,v in detalles.items(): lines.append(f'| {k} | {v} |')
    extras={k:v for k,v in encontrados.items() if k not in detalles}
    if extras:
        lines += ['', '## Verificaciones adicionales sin puntaje', '']
        for k,v in sorted(extras.items()): lines.append(f'- {k}: {", ".join(v)}')
    lines += ['', 'Las filas se muestran con dos decimales. El total se calcula antes de redondear.',
              'La evaluación oficial usa la copia controlada por el docente y revisión del código.']
    output = Path(output); output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text('\n'.join(lines)+'\n', encoding='utf-8')
    print('\n'.join(lines[:14]))
    print('Reporte:', output)
    ok = concluyente and total == Decimal(str(config['maximo'])) and all(v == ['aprobada'] for v in extras.values())
    return 0 if ok else 1

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--rubrica', type=Path, default=ROOT/'tools/rubrica.json')
    p.add_argument('--resultados', type=Path, default=ROOT/'build/test-results/test')
    p.add_argument('--salida', type=Path, default=ROOT/'build/evaluacion/reporte-calificacion.md')
    p.add_argument('--estado', choices=['ok','compilacion','infraestructura','ejecucion'], default='ok')
    a=p.parse_args()
    return generar(cargar_rubrica(a.rubrica),a.resultados,a.salida,a.estado)

if __name__=='__main__': raise SystemExit(main())
