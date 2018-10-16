package com.winterhaven_mc.deathchest;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Sign;

import static com.winterhaven_mc.deathchest.DeathChestBlock.*;


public class DeathChestBlockTest {


	/*
	 * Test Static isDeathChestSign
	 */
	
	@Test
	public void testIsDeathSign1() {
		
		// test when block type is not a wall sign or sign post
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathSign.",isDeathSign(mockBlock));

	}

	@Test
	public void testIsDeathSign2() {
		
		// test when block type is a wall sign without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
		
	@Test
	public void testIsDeathSign3() {

		// test when block type is a wall sign with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Wall sign with deathchest-owner metadata is a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign4() {
		
		// test when block type is a sign post without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign5() {
		
		// test when block type is a sign post with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Sign post with deathchest-owner metadata is a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign6() {
		
		// test when block type is a chest without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block with deathchest-owner metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign7() {
		
		// test when block type is a chest with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Chest block with deathchest-owner metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign8() {
		
		// test when passed null
		assertFalse("Return false when passed null.",isDeathSign(null));
	}


	@Test
	public void testIsDeathChest1() {

		// test when block type is not a chest or sign
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest2() {

		// test when block type is a wall sign without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest3() {

		// test when block type is a wall sign with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Wall sign with deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest4() {

		// test when block type is a sign post without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest5() {

		// test when block type is a sign post with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Sign post with deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest6() {

		// test when block type is a chest without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block without deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest7() {

		// test when block type is a chest with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Chest block with deathchest-owner metadata is a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest8() {

		// test when passed null
		assertFalse("Return false when passed null.",isDeathChest((Block) null));
	}

	
	@Test
	public void testIsDeathChestBlock1() {
		
		// test when block type is not a chest or sign
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock2() {
		
		// test when block type is a wall sign without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock3() {
		
		// test when block type is a wall sign with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Wall sign with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock4() {
		
		// test when block type is a sign post without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock5() {
		
		// test when block type is a sign post with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Sign post with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock6() {
		
		// test when block type is a chest without deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block without deathchest-owner metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock7() {
		
		// test when block type is a chest with deathchest metadata
		
		Block mockBlock = mock(Block.class);

		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Chest block with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock8() {
		
		// test when passed null
		assertFalse("Return false when passed null.",isDeathChestBlock(null));	
	}


	/*
	 * Test Static getAttachedBlock()
	 */
	
	@Test
	public void testStaticGetAttachedBlock1() {
		
		// test when passed null
		assertNull("Return null when passed null.",getAttachedBlock(null));
	}
	
	@Test
	public void testStaticGetAttachedBlock2() {

		// test when mockPassedBlock is a DeathSign (WALL_SIGN) and attached block is a DeathChest (mockBlock)
		
		Block mockPassedBlock = mock(Block.class);
		BlockState mockBlockState = mock(BlockState.class);
		Block mockReturnBlock = mock(Block.class);
		Sign mockSign = mock(Sign.class);

		when(mockPassedBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		when(mockPassedBlock.getState()).thenReturn(mockBlockState);
		when(mockBlockState.getData()).thenReturn(mockSign);

		when(mockSign.getAttachedFace()).thenReturn(BlockFace.NORTH);
		
		when(mockPassedBlock.getRelative(BlockFace.NORTH)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		
		assertTrue(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock3() {

		// test when mockPassedBlock is a wall sign without deathchest metadata
		
		Block mockPassedBlock = mock(Block.class);
		BlockState mockBlockState = mock(BlockState.class);
		Block mockReturnBlock = mock(Block.class);
		Sign mockSign = mock(Sign.class);

		when(mockPassedBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		when(mockPassedBlock.getState()).thenReturn(mockBlockState);
		when(mockBlockState.getData()).thenReturn(mockSign);

		when(mockSign.getAttachedFace()).thenReturn(BlockFace.NORTH);
		
		when(mockPassedBlock.getRelative(BlockFace.NORTH)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock4() {

		// test when mockPassedBlock is a DeathSign (WALL_SIGN)
		// but attached block is a chest without DeathChest metadata
		
		Block mockPassedBlock = mock(Block.class);
		BlockState mockBlockState = mock(BlockState.class);
		Block mockReturnBlock = mock(Block.class);
		Sign mockSign = mock(Sign.class);

		when(mockPassedBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		when(mockPassedBlock.getState()).thenReturn(mockBlockState);
		when(mockBlockState.getData()).thenReturn(mockSign);

		when(mockSign.getAttachedFace()).thenReturn(BlockFace.NORTH);
		
		when(mockPassedBlock.getRelative(BlockFace.NORTH)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock5() {

		// test when mockPassedBlock is a DeathSign (WALL_SIGN)
		// but attached block is not a chest
		
		Block mockPassedBlock = mock(Block.class);
		BlockState mockBlockState = mock(BlockState.class);
		Block mockReturnBlock = mock(Block.class);
		Sign mockSign = mock(Sign.class);

		when(mockPassedBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		when(mockPassedBlock.getState()).thenReturn(mockBlockState);
		when(mockBlockState.getData()).thenReturn(mockSign);

		when(mockSign.getAttachedFace()).thenReturn(BlockFace.NORTH);
		
		when(mockPassedBlock.getRelative(BlockFace.NORTH)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.DIRT);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock6() {

		// test when mockPassedBlock is a DeathSign (SIGN) and attached block is a DeathChest (mockBlock)
		
		Block mockPassedBlock = mock(Block.class);
		Block mockReturnBlock = mock(Block.class);

		when(mockPassedBlock.getType()).thenReturn(Material.SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);

		when(mockPassedBlock.getRelative(0,-1,0)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		
		assertTrue(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock7() {

		// test when mockPassedBlock is a sign post without DeathChest metadata
		
		Block mockPassedBlock = mock(Block.class);
		Block mockReturnBlock = mock(Block.class);

		when(mockPassedBlock.getType()).thenReturn(Material.SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(false);

		when(mockPassedBlock.getRelative(0,-1,0)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock8() {

		// test when mockPassedBlock is a DeathSign (SIGN)
		// but attached block is not a chest
		
		Block mockPassedBlock = mock(Block.class);
		Block mockReturnBlock = mock(Block.class);

		when(mockPassedBlock.getType()).thenReturn(Material.SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);

		when(mockPassedBlock.getRelative(0,-1,0)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.DIRT);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}
	
	@Test
	public void testStaticGetAttachedBlock9() {

		// test when mockPassedBlock is a DeathSign (SIGN)
		// but attached chest does not have DeathChest metadata
		
		Block mockPassedBlock = mock(Block.class);
		Block mockReturnBlock = mock(Block.class);

		when(mockPassedBlock.getType()).thenReturn(Material.SIGN);
		when(mockPassedBlock.hasMetadata("deathchest-owner")).thenReturn(true);

		when(mockPassedBlock.getRelative(0,-1,0)).thenReturn(mockReturnBlock);
		
		when(mockReturnBlock.getType()).thenReturn(Material.CHEST);
		when(mockReturnBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		
		assertFalse(isDeathChestBlock(getAttachedBlock(mockPassedBlock)));
	}

	
//	@Test
//	public void testGetInstance(Inventory inventory) {
//		
//		Inventory mockInventory = mock(Inventory.class);
//		InventoryHolder mockInventoryHolder = mock(InventoryHolder.class);
//		Block mockChestBlock = mock(Block.class);
//		
//		when(mockInventory.getHolder()).thenReturn(mockInventoryHolder);
//		when(mockInventoryHolder instanceof Block).thenReturn(true);
//		when((Block) inventory.getHolder()).thenReturn(mockChestBlock);
//		
//		assertTrue(isDeathChest(mockChestBlock));
//		
//	}
	
}
