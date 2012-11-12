<?php

 //BUILD THE FILE INFORMATION
 /*
     $filepath = "http://10.22.4.33/csp301/";
     $filename = "log-comm.00.out";
     $file = $filepath . $filename;
	$cmd = "wget -nv -r -nc -np -nH -R index.html http://10.22.4.33/csp301/ -o downloadLog.out";
	exec($cmd);
*/
/********************************/
/* Code at http://legend.ws/blog/tips-tricks/csv-php-mysql-import/
/* Edit the entries below to reflect the appropriate values
/********************************/
$databasehost = "localhost";
$databasename = "name";
$databasetable = "Age";
$databaseusername ="csp301";
$databasepassword = "";
$fieldseparator = ",";
$lineseparator = "\n";
$csvfile = "";
$logfile = "downloadLog.out";
$tableName;

if(!file_exists($logfile)) {
	echo "Log File not found. Make sure you specified the correct path.\n";
	exit;
}

$fileLog = fopen($logfile,"r");

if(!$fileLog) {
	echo "Error opening Log file.\n";
	exit;
}

$sizeLog = filesize($logfile);

if(!$sizeLog) {
	echo "Log File is empty.\n";
	exit;
}

$logcontent = fread($fileLog,$sizeLog);

fclose($fileLog);


$linesLog = 0;

$linearrayLog = array();

foreach(split($lineseparator,$logcontent) as $lineLog) {

	$linesLog++;

//	echo $linesLog;
//	echo "\n";
//	echo $lineLog;
//	echo "\n";
	
	if(strlen($lineLog))
	{
		if($linesLog>=4)
		{
			$tempArray = array();
			$tempArray = explode(" ",$lineLog,2);
			if(!(strcmp($tempArray[0],"FINISHED")&&strcmp($tempArray[0],"Downloaded:")))
			{
				echo "No new file downloaded\n";
			}
			else
			{
				$tempArray = explode("\"",$lineLog,3);
				$csvfile = $tempArray[1];
				parserLogs($csvfile);
				
			}
		}
	}
}

function parserLogs($csvfile)
{
	echo $csvfile."\n";
	
	
	$addauto = 0;
	/********************************/
	/* Would you like to save the mysql queries in a file? If yes set $save to 1.
	/* Permission on the file should be set to 777. Either upload a sample file through ftp and
	/* change the permissions, or execute at the prompt: touch output.sql && chmod 777 output.sql
	/********************************/
	$save = 1;
	//	$outputfile = "output.sql";
	/********************************/
	
	
	if(!file_exists("./".$csvfile)) {
		echo "File not found. Make sure you specified the correct path.\n";
		exit;
	}
	
	$file = fopen("./".$csvfile,"r");
	
	if(!$file) {
		echo "Error opening data file.\n";
		exit;
	}

	$size = filesize("./".$csvfile);
	
	if(!$size) {
		echo "File is empty.\n";
		exit;
	}
		
	$csvcontent = fread($file,$size);

	fclose($file);
	
	//echo $csvcontent;
	echo "File Found"."\n";
	//$con = @mysql_connect($databasehost,$databaseusername,$databasepassword) or die(mysql_error());
	//@mysql_select_db($databasename) or die(mysql_error());
	
	$lines = 0;
	//$linearray = array(11);
	$linearray = "";
	
	$mysqli = new mysqli("localhost", "csp301", "", "name");
	if ($mysqli->connect_errno) {
	    echo "Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error;
	}
	
	if (!$mysqli->query("DROP TABLE IF EXISTS temp") ||
	    !$mysqli->query("CREATE TABLE temp(edge VARCHAR(15), tag VARCHAR(20), date INT(2), month VARCHAR(3), year INT(4), day VARCHAR(3), time VARCHAR(8), timezone VARCHAR(3), milsec INT(20), node1 INT(4), node2 INT(4) )") ) {
	echo "Table creation failed: (" . $mysqli->errno . ") " . $mysqli->error;
	}
		
	/*$tt;
	foreach(split(" ",$csvcontent) as $tt)
	{
		echo $tt."\n";
	}
	*/
	foreach(explode("\n",$csvcontent) as $line)
	{
	
		$lines++;
		//echo $lines."\n";
		//$line = trim($line," \t");
		if(strlen($line))
		{
			$line = str_replace("\r","",$line);
			
			/************************************
			This line escapes the special character. remove it if entries are already escaped in the csv file
			************************************/
			$line = str_replace("'","\'",$line);
			/*************************************/
			
			$tempArray1 = array();
			$tempArray1 = explode(": ",$line,2);
			
			$stampEdgeTag = array();
			$stampEdgeTag = explode(", ",$tempArray1[1],3);
			$nodesSeparated = array();
			$nodesSeparated = explode("-",$stampEdgeTag[1],2);
			$stamp = array();
			$stamp = explode(" ",$stampEdgeTag[0],6);
			
			//$date = "";
		//$date = $stamp[5]."-".$stamp[1]."-".$stamp[2]." ".$stamp[3];
			
			$milsec = array();
			$milsec = explode("-",$tempArray1[0],3);
	
	
			$linearray = "'".$stampEdgeTag[1]."','".$stampEdgeTag[2]."',".$stamp[2].",'".$stamp[1]."',".$stamp[5].",'".$stamp[0]."','".$stamp[3]."','".$stamp[4]."',".$milsec[2].",".$nodesSeparated[0].",".$nodesSeparated[1];		
			$res = $mysqli->query("INSERT INTO temp VALUES(".$linearray.")");
			
		
		
		}
	}
	$r = $mysqli->query("DROP TABLE IF EXISTS daysData");
	$r = $mysqli->query("CREATE TABLE daysData(edge VARCHAR(15), tag VARCHAR(20), date INT(2), month VARCHAR(3), year INT(4), day VARCHAR(3), node1 INT(4), node2 INT(4), freq INT(10) ,UNIQUE INDEX id(edge,tag,date))");
	$r = $mysqli->query("INSERT INTO daysData SELECT edge, tag, date, month, year, day, node1, node2, COUNT(*) FROM temp GROUP BY edge, tag, date ");
	//$monthNames = array();
	$query = "SELECT DISTINCT month FROM daysData";

	$transMonth = 0;
	$row = array();
	$names = array();
	/* execute multi query */
	if ($mysqli->multi_query($query)) 
	{
		do
		{
	        /* store first result set */
	        if ($result = $mysqli->use_result()) 
	        {
	          	while ($row = $result->fetch_row()) 
	          	{
	                printf("%s\n", $row[0]);
	                $names[$transMonth] = $row[0];
				$transMonth++;
				}		
	        	    $result->close();
			}
     /* print divider */
		     if ($mysqli->more_results()) {
				printf("-----------------\n");
		     }
		 } while ($mysqli->next_result());
	}

	echo "\n value of transMonth = ".$transMonth."\n".$names[0]."\n".$names[1]."\n";


	$query = "SELECT DISTINCT month FROM currmonth";

	//$curr = 0;
	//$row = array();
	//$names = array();
	/* execute multi query */
	if ($mysqli->multi_query($query)) 
	{
		do
		{
	        /* store first result set */
	        if ($result = $mysqli->use_result()) 
	        {
	          	while ($row = $result->fetch_row()) 
	          	{
	                printf("%s\n", $row[0]);
	                $names[2] = $row[0];

				}		
	        	    $result->close();

			}
     /* print divider */
		     if ($mysqli->more_results()) {
				printf("-----------------\n");
		     }
		 } while ($mysqli->next_result());
	}

	//echo "\n value of transMonth = ".$transMonth."\n".$names[0]."\n".$names[1]."\n";

	echo "\n value of transMonth = ".$transMonth."\n".$names[0]."\n".$names[1]."\n".$names[2]."\n";

	
	if($transMonth == 1)
	{
		$rm = $mysqli->query("CREATE TABLE IF NOT EXISTS currmonth(edge VARCHAR(15), tag VARCHAR(20), month VARCHAR(3), year INT(4), node1 INT(4), node2 INT(4), freq INT(10) , UNIQUE INDEX id(edge,tag,month))");
		$rm = $mysqli->query("INSERT INTO currmonth SELECT edge, tag, month, year, node1, node2, freq FROM daysData ON DUPLICATE KEY UPDATE currmonth.freq = currmonth.freq + daysData.freq");
		
	}
	else
	{
		$rm = $mysqli->query("CREATE TABLE IF NOT EXISTS month1(edge VARCHAR(15), tag VARCHAR(20), month VARCHAR(3), year INT(4), node1 INT(4), node2 INT(4), freq INT(10) , UNIQUE INDEX id(edge,tag,month))");
		$rm = $mysqli->query("CREATE TABLE IF NOT EXISTS month2(edge VARCHAR(15), tag VARCHAR(20), month VARCHAR(3), year INT(4), node1 INT(4), node2 INT(4), freq INT(10) , UNIQUE INDEX id(edge,tag,month))");
		//$rm = $mysqli->query("CREATE TABLE IF NOT EXISTS month(edge VARCHAR(15), tag VARCHAR(20), month VARCHAR(3), year INT(4), node1 INT(4), node2 INT(4), freq INT(10) , UNIQUE INDEX id(edge,tag,month))");				
		$rm = $mysqli->query("DROP TABLE IF EXISTS month3");//Drop Month3
		$rm = $mysqli->query("RENAME TABLE month2 TO month3, month1 TO month2, currmonth TO month1");
		$rm = $mysqli->query("CREATE TABLE IF NOT EXISTS currmonth(edge VARCHAR(15), tag VARCHAR(20), month VARCHAR(3), year INT(4), node1 INT(4), node2 INT(4), freq INT(10) , UNIQUE INDEX id(edge,tag,month))");
		if(strcmp($names[0],$names[2])==0)
		{
			$rm = $mysqli->query("INSERT INTO month1 SELECT edge, tag, month, year, node1, node2, freq FROM daysData WHERE daysData.month='".$names[0]."' ON DUPLICATE KEY UPDATE month1.freq = month1.freq + daysData.freq");
			$rm = $mysqli->query("INSERT INTO currmonth SELECT edge, tag, month, year, node1, node2, freq FROM daysData WHERE daysData.month='".$names[1]."' ON DUPLICATE KEY UPDATE currmonth.freq = currmonth.freq + daysData.freq");
		}
		else
		{
			$rm = $mysqli->query("INSERT INTO month1 SELECT edge, tag, month, year, node1, node2, freq FROM daysData WHERE daysData.month='".$names[1]."' ON DUPLICATE KEY UPDATE month1.freq = month1.freq + daysData.freq");
			$rm = $mysqli->query("INSERT INTO currmonth SELECT edge, tag, month, year, node1, node2, freq FROM daysData WHERE daysData.month='".$names[0]."' ON DUPLICATE KEY UPDATE currmonth.freq = currmonth.freq + daysData.freq");
		
		}
	}
	
/* close connection */
$mysqli->close();

}	
//@mysql_close($con);



?>
