import copy
import json
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET
from decimal import Decimal
from calificar import cargar_rubrica, analizar, generar

class CalificadorTest(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory(); self.addCleanup(self.tmp.cleanup)
        self.root=Path(self.tmp.name)
        self.config=cargar_rubrica(Path(__file__).with_name('rubrica.json'))
        self.ids=[i for g in self.config['grupos'] for i in g['pruebas']]
    def xml(self, estados, nombre='TEST-casos.xml'):
        suite=ET.Element('testsuite')
        for id,estado in estados:
            cls,n=id.split('#'); c=ET.SubElement(suite,'testcase',classname=cls,name=n+'()')
            if estado!='ok': ET.SubElement(c,estado,type='org.opentest4j.AssertionFailedError')
        ET.ElementTree(suite).write(self.root/nombre,encoding='utf-8')
    def nota(self): return analizar(self.config,self.root)[1]
    def test_completo(self):
        self.xml([(i,'ok') for i in self.ids]);self.assertEqual(Decimal(10),self.nota())
    def test_todos_fallan(self):
        self.xml([(i,'failure') for i in self.ids]);self.assertEqual(0,self.nota())
    def test_parcial_por_pesos(self):
        seleccion=self.config['grupos'][0]['pruebas'][:9]+self.config['grupos'][1]['pruebas'][:4]+self.config['grupos'][2]['pruebas'][:7]
        self.xml([(i,'ok') for i in seleccion]);self.assertEqual(5,self.nota())
    def test_omitida(self):
        self.xml([(self.ids[0],'skipped')]);self.assertEqual(0,self.nota())
    def test_eliminada(self):
        self.xml([(i,'ok') for i in self.config['grupos'][1]['pruebas'][:4]])
        self.assertEqual(1,self.nota())
    def test_duplicada(self):
        self.xml([(self.ids[0],'ok'),(self.ids[0],'ok')]);self.assertEqual(0,self.nota())
    def test_error(self):
        self.xml([(self.ids[0],'error')]);self.assertEqual(0,self.nota())
    def test_sin_reportes(self):
        self.assertEqual(0,self.nota());self.assertTrue(all(x=='no ejecutada' for x in analizar(self.config,self.root)[2].values()))
    def test_infraestructura_no_definitiva(self):
        dest=self.root/'resultado.md';generar(self.config,self.root,dest,'infraestructura')
        self.assertIn('no concluyente',dest.read_text())
    def test_compilacion(self):
        dest=self.root/'resultado.md';generar(self.config,self.root,dest,'compilacion')
        self.assertIn('no compiló',dest.read_text())
    def test_xml_malformado(self):
        (self.root/'TEST-error.xml').write_text('<testsuite>')
        self.assertTrue(analizar(self.config,self.root)[3]);self.assertEqual(0,self.nota())
    def test_rubrica_duplicada(self):
        conf=copy.deepcopy(self.config);conf['grupos'][0]['pruebas'].append(self.ids[0])
        p=self.root/'rubrica.json';p.write_text(json.dumps(conf))
        with self.assertRaises(ValueError): cargar_rubrica(p)
    def test_rubrica_pesos(self):
        conf=copy.deepcopy(self.config);conf['grupos'][0]['maximo']=-1
        p=self.root/'rubrica.json';p.write_text(json.dumps(conf))
        with self.assertRaises(ValueError): cargar_rubrica(p)
    def test_no_redondeo_intermedio(self):
        self.xml([(self.ids[0],'ok')]);self.assertEqual(Decimal(1)/Decimal(18)*4,self.nota())

if __name__=='__main__': unittest.main()
