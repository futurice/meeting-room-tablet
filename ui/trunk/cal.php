<table>
<?php
    for($j=8; $j<18; $j++){
?>
<tr>
  <th class="hour"><?= $j ?></th>
  <?php
  for($i=0; $i<5; $i++){
  ?>
    <td class="hour d<?= $i ?>-<?=$j ?>-00"></td>
  <?php
  }
?>  
</tr>
<tr>
  <th class="hour"><?= $j ?>.30</th>
  <?php
    for($i=0; $i<5; $i++){
  ?>
    <td class="hour d<?= $i ?>-<?=$j ?>-30"></td>  
<?php
  }
?>
</tr>
<?php
}
?>
</table>