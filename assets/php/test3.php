<?php

if(isset($_GET['dateBegin']) & isset($_GET['dateEnd'])) {

	$dateBegin=$_GET['dateBegin'];
	$dateEnd=$_GET['dateEnd'];

	$connect = mysql_connect('localhost', 'sadmin', 'sadmin');
	if(!$connect) {
		die('Could not connect:' . mysql_error() );
	}

	mysql_select_db ('serverroom', $connect);

	$result = mysql_query("SELECT `hms`, `temp` FROM `t1` WHERE `hms` BETWEEN $dateBegin AND $dateEnd ORDER BY `hms` ") or die ("invalid query ".mysql_error());

	$dom = new domDocument("1.0", "utf-8");
	$rootProject = $dom->createElement("project");
	$rootProject->setAttribute("name","Thermo1");
	$rootResult = $dom->createElement("result");

while ($data = mysql_fetch_array($result)){	
	
	$rootPoint =$dom->createElement("point");
	$rootPoint->setAttribute("date", $data['hms']);
	$rootPoint->appendChild($dom->createTextNode($data['temp']));
	
	$rootResult->appendChild($rootPoint);
}

$rootProject->appendChild($rootResult);
$dom->appendChild($rootProject);

echo $dom->saveXML();

mysql_close($connect);

} else {
	die('No params.');
}

?>